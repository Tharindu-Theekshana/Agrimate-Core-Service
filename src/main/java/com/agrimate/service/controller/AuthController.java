package com.agrimate.service.controller;

import com.agrimate.service.dto.AuthDtos.AuthResponse;
import com.agrimate.service.dto.AuthDtos.LoginRequest;
import com.agrimate.service.dto.AuthDtos.RefreshRequest;
import com.agrimate.service.dto.AuthDtos.RegisterOtpRequest;
import com.agrimate.service.dto.AuthDtos.RegisterRequest;
import com.agrimate.service.dto.AuthDtos.ResetPasswordRequest;
import com.agrimate.service.dto.AuthDtos.SendOtpRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String REFRESH_COOKIE = "agrimate_refresh";

    private final AuthService authService;
    private final Validator validator;
    private final long refreshTtlDays;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthController(AuthService authService, Validator validator,
                          @Value("${agrimate.jwt.refresh-ttl-days:7}") long refreshTtlDays,
                          @Value("${agrimate.auth.cookie-secure:false}") boolean cookieSecure,
                          @Value("${agrimate.auth.cookie-same-site:Lax}") String cookieSameSite) {
        this.authService = authService;
        this.validator = validator;
        this.refreshTtlDays = refreshTtlDays;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }
    
    @PostMapping("/register/request-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestRegisterOtp(@Valid @RequestBody RegisterOtpRequest req) {
        authService.requestRegistrationOtp(req.username(), req.email());
    }

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public AuthResponse register(@RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String location,
                                 @RequestParam(required = false) RoleName role,
                                 @RequestParam("code") String code,
                                 @RequestParam(value = "proofImage", required = false) MultipartFile proofImage,
                                 HttpServletResponse res) {
        RegisterRequest req = new RegisterRequest(username, email, password, name, phone, location, role);
        validate(req);
        AuthResponse auth = authService.register(req, code, proofImage);
        setRefreshCookie(res, auth.refreshToken());
        return auth;
    }

    private void validate(RegisterRequest req) {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw ApiException.badRequest(message);
        }
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        AuthResponse auth = authService.login(req);
        setRefreshCookie(res, auth.refreshToken());
        return auth;
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody(required = false) RefreshRequest body,
                                @CookieValue(value = REFRESH_COOKIE, required = false) String cookieToken,
                                HttpServletResponse res) {
        String token = (cookieToken != null && !cookieToken.isBlank())
                ? cookieToken
                : (body != null ? body.refreshToken() : null);
        AuthResponse auth = authService.refresh(token);
        setRefreshCookie(res, auth.refreshToken()); // rotate
        return auth;
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse res) {
        clearRefreshCookie(res);
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@Valid @RequestBody SendOtpRequest req) {
        authService.requestPasswordReset(req.email());
    }

    @PostMapping("/password-reset/confirm")
    public AuthResponse confirmPasswordReset(@Valid @RequestBody ResetPasswordRequest req, HttpServletResponse res) {
        AuthResponse auth = authService.confirmPasswordReset(req.email(), req.code(), req.newPassword());
        setRefreshCookie(res, auth.refreshToken());
        return auth;
    }

    private void setRefreshCookie(HttpServletResponse res, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
