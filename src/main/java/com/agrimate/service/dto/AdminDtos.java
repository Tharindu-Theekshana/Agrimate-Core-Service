package com.agrimate.service.dto;

import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.notification.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public final class AdminDtos {
    private AdminDtos() {}

    public record BroadcastRequest(
            @NotBlank String title,
            String body,
            NotificationType type,
            Long userId
    ) {}

    public record CreateAdminRequest(
            @NotBlank @Size(min = 3, max = 30)
            @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, numbers, . _ -")
            String username,
            @NotBlank @Email String email,
            @NotBlank String name,
            String phone,
            String location
    ) {}

    public record OutbreakPoint(
            Long scanId,
            String disease,
            double confidence,
            double latitude,
            double longitude,
            String createdAt
    ) {}

    public record UpdateUserStatusRequest(
            AgronomistStatus agronomistStatus,
            Boolean suspended
    ) {}

    public record Analytics(
            long totalScans,
            long totalUsers,
            long totalFarmers,
            long pendingAgronomists,
            Map<String, Long> scansByDisease,
            List<WeeklyPoint> weeklyTrend
    ) {}

    public record WeeklyPoint(String weekStart, long scans) {}
}
