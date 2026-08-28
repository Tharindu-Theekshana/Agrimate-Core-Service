package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.otp.OtpPurpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Duration resendCooldown;

    public OtpService(@Value("${agrimate.otp.ttl-minutes:10}") long ttlMinutes,
                      @Value("${agrimate.otp.resend-cooldown-seconds:15}") long resendCooldownSeconds) {
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.resendCooldown = Duration.ofSeconds(resendCooldownSeconds);
    }

    public String issue(String email, OtpPurpose purpose) {
        String key = key(email, purpose);
        Entry existing = store.get(key);
        if (existing != null && existing.createdAt.isAfter(Instant.now().minus(resendCooldown))) {
            throw ApiException.badRequest("Please wait a moment before requesting another code");
        }

        String code = generateCode();
        store.put(key, new Entry(code, Instant.now(), Instant.now().plus(ttl)));
        return code;
    }

    public void verify(String email, OtpPurpose purpose, String code) {
        String key = key(email, purpose);
        Entry entry = store.get(key);
        if (entry == null || !entry.code.equals(code) || entry.expiresAt.isBefore(Instant.now())) {
            throw ApiException.badRequest("Invalid or expired code");
        }
        store.remove(key);
    }

    public int ttlMinutes() {
        return (int) ttl.toMinutes();
    }

    @Scheduled(fixedRate = 15, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void evictExpired() {
        Instant now = Instant.now();
        store.values().removeIf(entry -> entry.expiresAt.isBefore(now));
    }

    private String key(String email, OtpPurpose purpose) {
        return purpose.name() + ":" + email.toLowerCase();
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private record Entry(String code, Instant createdAt, Instant expiresAt) {}
}
