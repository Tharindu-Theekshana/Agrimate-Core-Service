package com.agrimate.service.dto;

import com.agrimate.service.model.cropStageLog.CropStageLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public final class CropStageLogDtos {
    private CropStageLogDtos() {}

    public record CropStageLogRequest(
            @NotBlank String stageKey,
            @NotNull LocalDate reachedDate
    ) {}

    public record CropStageLogDto(
            Long id,
            Long cropId,
            String stageKey,
            LocalDate reachedDate
    ) {
        public static CropStageLogDto from(CropStageLog l) {
            return new CropStageLogDto(l.getId(), l.getCrop().getId(), l.getStageKey(), l.getReachedDate());
        }
    }
}
