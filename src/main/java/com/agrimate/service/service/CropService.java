package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.CropDtos.CropDto;
import com.agrimate.service.dto.CropDtos.CropRequest;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CropService {

    private final CropRepository cropRepository;
    private final FarmRepository farmRepository;

    public CropService(CropRepository cropRepository, FarmRepository farmRepository) {
        this.cropRepository = cropRepository;
        this.farmRepository = farmRepository;
    }

    public List<CropDto> listByFarm(User user, Long farmId) {
        ownedFarm(user, farmId);
        return cropRepository.findByFarmId(farmId).stream().map(CropDto::from).toList();
    }

    @Transactional
    public CropDto create(User user, Long farmId, CropRequest req) {
        Farm farm = ownedFarm(user, farmId);
        Crop crop = new Crop();
        crop.setFarm(farm);
        crop.setCropType("paddy");
        apply(crop, req);
        return CropDto.from(cropRepository.save(crop));
    }

    @Transactional
    public CropDto update(User user, Long cropId, CropRequest req) {
        Crop crop = ownedCrop(user, cropId);
        apply(crop, req);
        return CropDto.from(cropRepository.save(crop));
    }

    @Transactional
    public void delete(User user, Long cropId) {
        cropRepository.delete(ownedCrop(user, cropId));
    }

    private void apply(Crop crop, CropRequest req) {
        crop.setVariety(req.variety());
        crop.setSeason(req.season());
        crop.setAreaAcres(req.areaAcres());
        crop.setPlantingDate(req.plantingDate());
        if (req.expectedHarvestDate() != null) {
            crop.setExpectedHarvestDate(req.expectedHarvestDate());
        } else if (req.plantingDate() != null) {
            crop.setExpectedHarvestDate(req.plantingDate().plusDays(105));
        }
        crop.setGrowthStage(req.growthStage());
        if (req.status() != null) crop.setStatus(req.status());
        crop.setHarvestDate(req.harvestDate());
        crop.setYieldKg(req.yieldKg());
        crop.setQualityGrade(req.qualityGrade());
        crop.setSellingPrice(req.sellingPrice());
    }

    private Farm ownedFarm(User user, Long farmId) {
        return farmRepository.findByIdAndAccountId(farmId, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Farm not found"));
    }

    private Crop ownedCrop(User user, Long cropId) {
        return cropRepository.findByIdAndFarmAccountId(cropId, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Crop not found"));
    }
}
