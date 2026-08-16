package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.AuthDtos.AuthResponse;
import com.agrimate.service.dto.AuthDtos.LoginRequest;
import com.agrimate.service.dto.AuthDtos.RegisterRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.RoleRepository;
import com.agrimate.service.repository.UserRepository;
import com.agrimate.service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim();
        String email = req.email().trim();

        if (userRepository.existsByUsername(username)) throw ApiException.conflict("Username is already taken");
        if (userRepository.existsByEmail(email)) throw ApiException.conflict("Email is already registered");
        if (req.phone() != null && !req.phone().isBlank() && accountRepository.existsByPhone(req.phone().trim())) {
            throw ApiException.conflict("Phone number is already registered");
        }

        RoleName requested = req.role() == RoleName.AGRONOMIST ? RoleName.AGRONOMIST : RoleName.FARMER;

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));

        Account account = new Account();
        account.setUser(user);
        account.setName(req.name().trim());
        account.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        account.setLocation(req.location());
        account.setAgronomistStatus(requested == RoleName.AGRONOMIST ? AgronomistStatus.PENDING : AgronomistStatus.NONE);
        user.setAccount(account);

        Role role = roleRepository.findByName(requested)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Role not provisioned: " + requested));
        user.getUserRoles().add(new UserRole(user, role));

        user = userRepository.save(user);
        return tokensFor(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findDetailByUsernameOrEmail(req.identifier().trim())
                .orElseThrow(() -> ApiException.badRequest("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
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
