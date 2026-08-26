package com.agrimate.service.dto;

import java.time.Instant;
import java.util.List;

public record ScanDto(
        Long id,
        String imageUrl,
        String predictedDisease,
        double confidence,
        List<PredictionDto> top3,
        DiseaseDto disease,
        boolean lowConfidence,
        boolean modelMocked,
        Long farmId,
        Long cropId,
        Double latitude,
        Double longitude,
        Instant createdAt
) {}
