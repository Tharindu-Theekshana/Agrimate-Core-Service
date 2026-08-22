package com.agrimate.service.security;

import com.agrimate.service.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtService(
            @Value("${agrimate.jwt.secret}") String secret,
            @Value("${agrimate.jwt.access-ttl-minutes:60}") long accessTtlMinutes,
            @Value("${agrimate.jwt.refresh-ttl-days:30}") long refreshTtlDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlMs = accessTtlMinutes * 60_000;
        this.refreshTtlMs = refreshTtlDays * 24 * 60 * 60_000;
    }

    public String generateAccessToken(User user) {
        return build(user, "access", accessTtlMs);
    }

    public String generateRefreshToken(User user) {
        return build(user, "refresh", refreshTtlMs);
    }

    private String build(User user, String type, long ttlMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("roles", user.getRoleNames().stream().map(Enum::name).toList())
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parse(token).get("type", String.class));
    }
}
