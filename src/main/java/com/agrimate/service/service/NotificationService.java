package com.agrimate.service.service;

import com.agrimate.service.dto.NotificationDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;

    public NotificationService(NotificationRepository notificationRepository, AccountRepository accountRepository) {
        this.notificationRepository = notificationRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        Instant readAt = account.getNotificationsReadAt();

        List<NotificationDto> merged = new ArrayList<>();
        for (Notification n : notificationRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())) {
            merged.add(NotificationDto.from(n, isRead(n, readAt)));
        }
        for (Notification n : notificationRepository.findByAccountIsNullOrderByCreatedAtDesc()) {
            merged.add(NotificationDto.from(n, isRead(n, readAt)));
        }
        merged.sort(Comparator.comparing(NotificationDto::createdAt).reversed());
        return merged;
    }

    private boolean isRead(Notification n, Instant readAt) {
        return readAt != null && !n.getCreatedAt().isAfter(readAt);
    }

    @Transactional
    public void markAllRead(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        account.setNotificationsReadAt(Instant.now());
        accountRepository.save(account);
    }
}
