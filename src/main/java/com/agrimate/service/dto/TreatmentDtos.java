package com.agrimate.service.dto;

import com.agrimate.service.model.treatmentLog.TreatmentLog;
import com.agrimate.service.model.treatmentLog.TreatmentType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public final class TreatmentDtos {
    private TreatmentDtos() {}

    public record TreatmentRequest(
            @NotBlank String productName,
            TreatmentType type,
            String quantity,
            LocalDate appliedDate
    ) {}

    public record TreatmentDto(
            Long id,
            Long cropId,
            String productName,
            TreatmentType type,
            String quantity,
            LocalDate appliedDate
    ) {
        public static TreatmentDto from(TreatmentLog t) {
            return new TreatmentDto(t.getId(), t.getCrop().getId(), t.getProductName(), t.getType(),
                    t.getQuantity(), t.getAppliedDate());
        }
    }
}
