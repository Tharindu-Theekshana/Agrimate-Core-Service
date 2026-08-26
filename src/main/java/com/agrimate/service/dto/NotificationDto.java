package com.agrimate.service.dto;

import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.notification.NotificationType;

import java.time.Instant;

public record NotificationDto(
        Long id,
        NotificationType type,
        String title,
        String body,
        boolean read,
        Instant createdAt
) {
    public static NotificationDto from(Notification n, boolean read) {
        return new NotificationDto(n.getId(), n.getType(), n.getTitle(), n.getBody(), read, n.getCreatedAt());
    }
}
