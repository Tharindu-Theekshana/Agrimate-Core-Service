package com.agrimate.service.dto;

import com.agrimate.service.model.farm.Farm;
import jakarta.validation.constraints.NotBlank;

public final class FarmDtos {
    private FarmDtos() {}

    public record FarmRequest(
            @NotBlank String name,
            Double latitude,
            Double longitude,
            Double sizeAcres,
            String soilType
    ) {}

    public record FarmDto(
            Long id,
            String name,
            Double latitude,
            Double longitude,
            Double sizeAcres,
            String soilType
    ) {
        public static FarmDto from(Farm f) {
            return new FarmDto(f.getId(), f.getName(), f.getLatitude(), f.getLongitude(),
                    f.getSizeAcres(), f.getSoilType());
        }
    }
}
