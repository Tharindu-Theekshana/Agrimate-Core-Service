package com.agrimate.service.controller;

import com.agrimate.service.dto.AuthDtos.AuthResponse;
import com.agrimate.service.dto.AuthDtos.LoginRequest;
import com.agrimate.service.dto.AuthDtos.RefreshRequest;
import com.agrimate.service.dto.AuthDtos.RegisterRequest;
import com.agrimate.service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String REFRESH_COOKIE = "agrimate_refresh";

    private final AuthService authService;
    private final long refreshTtlDays;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthController(AuthService authService,
                          @Value("${agrimate.jwt.refresh-ttl-days:7}") long refreshTtlDays,
                          @Value("${agrimate.auth.cookie-secure:false}") boolean cookieSecure,
                          @Value("${agrimate.auth.cookie-same-site:Lax}") String cookieSameSite) {
        this.authService = authService;
        this.refreshTtlDays = refreshTtlDays;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req, HttpServletResponse res) {
        AuthResponse auth = authService.register(req);
        setRefreshCookie(res, auth.refreshToken());
        return auth;
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
