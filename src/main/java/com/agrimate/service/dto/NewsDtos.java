package com.agrimate.service.dto;

import com.agrimate.service.model.news.News;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class NewsDtos {
    private NewsDtos() {}

    public record NewsRequest(
            @NotBlank String title,
            @NotBlank String description,
            String imageUrl
    ) {}

    public record NewsDto(
            Long id,
            String title,
            String description,
            String imageUrl,
            Instant createdAt
    ) {
        public static NewsDto from(News n) {
            return new NewsDto(n.getId(), n.getTitle(), n.getDescription(), n.getImageUrl(), n.getCreatedAt());
        }
    }
}
