package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.AuthDtos.AuthResponse;
import com.agrimate.service.dto.AuthDtos.LoginRequest;
import com.agrimate.service.dto.AuthDtos.RegisterRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.event.UserRegisteredEvent;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.otp.OtpPurpose;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.RoleRepository;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StorageService storageService;
    private final OtpService otpService;
    private final MailService mailService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       StorageService storageService, OtpService otpService, MailService mailService,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.storageService = storageService;
        this.otpService = otpService;
        this.mailService = mailService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public void requestRegistrationOtp(String username, String email) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim();
        if (userRepository.existsByUsername(normalizedUsername)) throw ApiException.conflict("Username is already taken");
        if (userRepository.existsByEmail(normalizedEmail)) throw ApiException.conflict("Email is already registered");

        String code = otpService.issue(normalizedEmail, OtpPurpose.REGISTRATION);
        String html = EmailTemplates.otpEmail(
                "Verify your email",
                "Use the code below to verify your email and finish creating your AgriMate account.",
                code, otpService.ttlMinutes());
        mailService.send(normalizedEmail, "Verify your email to join AgriMate", html);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req, String code, MultipartFile proofImage, boolean callerIsAdmin) {
        String username = req.username().trim();
        String email = req.email().trim();

        if (userRepository.existsByUsername(username)) throw ApiException.conflict("Username is already taken");
        if (userRepository.existsByEmail(email)) throw ApiException.conflict("Email is already registered");
        if (req.phone() != null && !req.phone().isBlank() && accountRepository.existsByPhone(req.phone().trim())) {
            throw ApiException.conflict("Phone number is already registered");
        }
        if (req.role() == RoleName.ADMIN && !callerIsAdmin) {
            throw ApiException.forbidden("Only an administrator can create an admin account");
        }
        otpService.verify(email, OtpPurpose.REGISTRATION, code);

        RoleName requested = req.role() == RoleName.ADMIN ? RoleName.ADMIN
                : req.role() == RoleName.AGRONOMIST ? RoleName.AGRONOMIST
                : RoleName.FARMER;
        boolean hasProof = proofImage != null && !proofImage.isEmpty();
        if (requested == RoleName.AGRONOMIST && !hasProof) {
            throw ApiException.badRequest("A proof image is required to register as an agronomist");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));

        Account account = new Account();
        account.setUser(user);
        account.setName(req.name().trim());
        account.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        account.setLocation(req.location());
        account.setAccountType(requested);
        account.setAgronomistStatus(requested == RoleName.AGRONOMIST ? AgronomistStatus.PENDING : AgronomistStatus.NONE);
        if (requested == RoleName.AGRONOMIST) {
            account.setAgronomistProofUrl(storageService.upload(proofImage, "agrimate/agronomist-proofs"));
        }
        user.setAccount(account);

        Role role = roleRepository.findByName(requested)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Role not provisioned: " + requested));
        user.getUserRoles().add(new UserRole(user, role));

        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), account.getName()));
        return tokensFor(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String normalized = email.trim();
        userRepository.findDetailByUsernameOrEmail(normalized).ifPresent(user -> {
            String code = otpService.issue(normalized, OtpPurpose.PASSWORD_RESET);
            String html = EmailTemplates.otpEmail(
                    "Reset your password",
                    "Use the code below to reset your AgriMate account password.",
                    code, otpService.ttlMinutes());
            mailService.send(normalized, "Reset your AgriMate password", html);
        });
    }

    @Transactional
    public AuthResponse confirmPasswordReset(String email, String code, String newPassword) {
        String normalized = email.trim();
        User user = userRepository.findDetailByUsernameOrEmail(normalized)
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired code"));
        otpService.verify(normalized, OtpPurpose.PASSWORD_RESET, code);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return tokensFor(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findDetailByUsernameOrEmail(req.identifier().trim())
                .orElseThrow(() -> ApiException.badRequest("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw ApiException.badRequest("Invalid credentials");
        }
        if (user.getAccount() != null && user.getAccount().isSuspended()) {
            throw ApiException.forbidden("Your account has been suspended");
        }
        return tokensFor(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw ApiException.badRequest("Missing refresh token");
        }
        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw ApiException.badRequest("Not a refresh token");
            }
            Long userId = jwtService.extractUserId(refreshToken);
            User user = userRepository.findDetailById(userId)
                    .orElseThrow(() -> ApiException.badRequest("Invalid refresh token"));
            return tokensFor(user);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid or expired refresh token");
        }
    }

    private AuthResponse tokensFor(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                UserDto.from(user));
    }
}
