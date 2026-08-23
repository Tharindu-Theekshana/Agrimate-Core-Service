package com.agrimate.service.dto;

import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.crop.CropStatus;
import com.agrimate.service.model.crop.Season;

import java.time.LocalDate;

public final class CropDtos {
    private CropDtos() {}

    public record CropRequest(
            String variety,
            Season season,
            Double areaAcres,
            LocalDate plantingDate,
            LocalDate expectedHarvestDate,
            String growthStage,
            CropStatus status,
            LocalDate harvestDate,
            Double yieldKg,
            String qualityGrade,
            Double sellingPrice
    ) {}

    public record CropDto(
            Long id,
            Long farmId,
            String cropType,
            String variety,
            Season season,
            Double areaAcres,
            LocalDate plantingDate,
            LocalDate expectedHarvestDate,
            String growthStage,
            CropStatus status,
            LocalDate harvestDate,
            Double yieldKg,
            String qualityGrade,
            Double sellingPrice
    ) {
        public static CropDto from(Crop c) {
            return new CropDto(c.getId(), c.getFarm().getId(), c.getCropType(), c.getVariety(), c.getSeason(),
                    c.getAreaAcres(), c.getPlantingDate(), c.getExpectedHarvestDate(), c.getGrowthStage(),
                    c.getStatus(), c.getHarvestDate(), c.getYieldKg(), c.getQualityGrade(), c.getSellingPrice());
        }
    }
}
