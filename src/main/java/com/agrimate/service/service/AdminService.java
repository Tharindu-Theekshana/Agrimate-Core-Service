package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.AdminDtos.Analytics;
import com.agrimate.service.dto.AdminDtos.OutbreakPoint;
import com.agrimate.service.dto.AdminDtos.UpdateUserStatusRequest;
import com.agrimate.service.dto.AdminDtos.BroadcastRequest;
import com.agrimate.service.dto.AdminDtos.WeeklyPoint;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.scan.Scan;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.notification.NotificationType;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.NotificationRepository;
import com.agrimate.service.repository.ScanRepository;
import com.agrimate.service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ScanRepository scanRepository;
    private final NotificationRepository notificationRepository;
    private final PushService pushService;

    public AdminService(UserRepository userRepository, AccountRepository accountRepository,
                        ScanRepository scanRepository, NotificationRepository notificationRepository,
                        PushService pushService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.scanRepository = scanRepository;
        this.notificationRepository = notificationRepository;
        this.pushService = pushService;
    }

    @Transactional
    public int broadcast(BroadcastRequest req) {
        NotificationType type = req.type() != null ? req.type() : NotificationType.SYSTEM;

        if (req.userId() != null) {
            Account target = accountRepository.findByUserId(req.userId())
                    .orElseThrow(() -> ApiException.notFound("User not found"));

            Notification n = new Notification();
            n.setAccount(target);
            n.setType(type);
            n.setTitle(req.title());
            n.setBody(req.body());
            notificationRepository.save(n);

            pushService.sendToTokens(new ArrayList<>(target.getDeviceTokens().keySet()), req.title(), req.body(), type.name());
            return 1;
        }

        Notification n = new Notification();
        n.setAccount(null);
        n.setType(type);
        n.setTitle(req.title());
        n.setBody(req.body());
        notificationRepository.save(n);

        List<Account> accounts = accountRepository.findAll();
        List<String> tokens = new ArrayList<>();
        for (Account account : accounts) {
            tokens.addAll(account.getDeviceTokens().keySet());
        }
        pushService.sendToTokens(tokens, req.title(), req.body(), type.name());
        return accounts.size();
    }

    @Transactional(readOnly = true)
    public List<OutbreakPoint> outbreaks(String disease, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        return scanRepository.findGeoTaggedSince(since).stream()
                .filter(s -> disease == null || disease.isBlank() || disease.equals(s.getPredictedDisease()))
                .map(this::toPoint)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> users(RoleName role) {
        List<User> users = (role == null) ? userRepository.findAllDetailed() : userRepository.findAllByRole(role);
        return users.stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserStatusRequest req) {
        Account account = accountRepository.findByUserId(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (req.agronomistStatus() != null && req.agronomistStatus() != account.getAgronomistStatus()) {
            account.setAgronomistStatus(req.agronomistStatus());
            accountRepository.save(account);
            notifyAgronomistStatus(account, req.agronomistStatus());
        }
        if (req.suspended() != null && req.suspended() != account.isSuspended()) {
            account.setSuspended(req.suspended());
            accountRepository.save(account);
            notifySuspensionChange(account, req.suspended());
        }
        return UserDto.from(userRepository.findDetailById(id).orElseThrow());
    }

    private void notifyAgronomistStatus(Account account, AgronomistStatus status) {
        if (status == AgronomistStatus.APPROVED) {
            notify(account, NotificationType.AGRONOMIST_APPROVED, "Agronomist application approved",
                    "Congratulations! Your agronomist account has been approved. You can now answer farmers' questions.");
        } else if (status == AgronomistStatus.REJECTED) {
            notify(account, NotificationType.AGRONOMIST_REJECTED, "Agronomist application rejected",
                    "Your agronomist application was not approved. Contact support for more information.");
        }
    }

    private void notifySuspensionChange(Account account, boolean suspended) {
        if (suspended) {
            notify(account, NotificationType.ACCOUNT_SUSPENDED, "Your account has been suspended",
                    "Your AgriMate account has been suspended by an administrator. Contact support for more information.");
        } else {
            notify(account, NotificationType.ACCOUNT_REACTIVATED, "Your account has been reactivated",
                    "Your AgriMate account has been reactivated. You can sign in as normal.");
        }
    }

    private void notify(Account account, NotificationType type, String title, String body) {
        Notification n = new Notification();
        n.setAccount(account);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        notificationRepository.save(n);
        pushService.sendToTokens(new ArrayList<>(account.getDeviceTokens().keySet()), title, body, type.name());
    }

    @Transactional(readOnly = true)
    public Analytics analytics() {
        long totalScans = scanRepository.count();
        long totalUsers = userRepository.count();
        long totalFarmers = userRepository.findAllByRole(RoleName.FARMER).size();
        long pendingAgronomists = userRepository.countByAgronomistStatus(AgronomistStatus.PENDING);

        Map<String, Long> byDisease = new LinkedHashMap<>();
        scanRepository.countGroupedByDisease().forEach(dc -> byDisease.put(dc.getDisease(), dc.getTotal()));

        return new Analytics(totalScans, totalUsers, totalFarmers, pendingAgronomists, byDisease, weeklyTrend());
    }

    private List<WeeklyPoint> weeklyTrend() {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        Map<LocalDate, Long> counts = new TreeMap<>();
        Instant since = Instant.now().minus(56, ChronoUnit.DAYS);
        for (Scan s : scanRepository.findByCreatedAtAfter(since)) {
            LocalDate d = LocalDate.ofInstant(s.getCreatedAt(), ZoneOffset.UTC);
            LocalDate weekStart = d.with(wf.dayOfWeek(), 1);
            counts.merge(weekStart, 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .map(e -> new WeeklyPoint(e.getKey().toString(), e.getValue()))
                .toList();
    }

    private OutbreakPoint toPoint(Scan s) {
        return new OutbreakPoint(s.getId(), s.getPredictedDisease(), s.getConfidence(),
                s.getLatitude(), s.getLongitude(),
                s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
    }
}
