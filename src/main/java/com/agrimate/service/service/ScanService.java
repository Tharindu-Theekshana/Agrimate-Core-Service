package com.agrimate.service.service;

import com.agrimate.service.client.MlClient;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.DiseaseDto;
import com.agrimate.service.dto.PredictionDto;
import com.agrimate.service.dto.ScanDto;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.disease.Disease;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.scan.Scan;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.DiseaseRepository;
import com.agrimate.service.repository.FarmRepository;
import com.agrimate.service.repository.ScanRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ScanService {

    private final ScanRepository scanRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final DiseaseRepository diseaseRepository;
    private final StorageService storageService;
    private final MlClient mlClient;
    private final ObjectMapper objectMapper;
    private final double confidenceThreshold;

    public ScanService(ScanRepository scanRepository, FarmRepository farmRepository,
                       CropRepository cropRepository, DiseaseRepository diseaseRepository,
                       StorageService storageService, MlClient mlClient, ObjectMapper objectMapper,
                       @Value("${agrimate.scan.low-confidence-threshold:0.6}") double confidenceThreshold) {
        this.scanRepository = scanRepository;
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.diseaseRepository = diseaseRepository;
        this.storageService = storageService;
        this.mlClient = mlClient;
        this.objectMapper = objectMapper;
        this.confidenceThreshold = confidenceThreshold;
    }

    @Transactional
    public ScanDto scan(User user, MultipartFile image, Long farmId, Long cropId,
                        Double latitude, Double longitude) {
        if (image == null || image.isEmpty()) {
            throw ApiException.badRequest("An image is required");
        }

        Farm farm = resolveFarm(user, farmId);
        Crop crop = resolveCrop(user, cropId);

        String imageUrl = storageService.upload(image);

        MlClient.MlResult ml = uploadToMl(image);
        List<PredictionDto> top3 = ml.toDtos();
        if (top3.isEmpty()) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY, "No prediction returned");
        }
        PredictionDto top = top3.get(0);

        Disease disease = diseaseRepository.findByDiseaseKey(top.disease()).orElse(null);

        Scan scan = new Scan();
        scan.setAccount(user.getAccount());
        scan.setFarm(farm);
        scan.setCrop(crop);
        scan.setImageUrl(imageUrl);
        scan.setPredictedDisease(top.disease());
        scan.setConfidence(top.confidence());
        scan.setTop3Json(writeJson(top3));
        scan.setLatitude(latitude != null ? latitude : (farm != null ? farm.getLatitude() : null));
        scan.setLongitude(longitude != null ? longitude : (farm != null ? farm.getLongitude() : null));
        scan = scanRepository.save(scan);

        return toDto(scan, top3, disease, ml.modelLoaded());
    }

    public ScanDto scanGuest(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw ApiException.badRequest("An image is required");
        }
        MlClient.MlResult ml = uploadToMl(image);
        List<PredictionDto> top3 = ml.toDtos();
        if (top3.isEmpty()) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY, "No prediction returned");
        }
        PredictionDto top = top3.get(0);
        Disease disease = diseaseRepository.findByDiseaseKey(top.disease()).orElse(null);
        return new ScanDto(
                null, null, top.disease(), top.confidence(), top3,
                disease != null ? DiseaseDto.from(disease) : null,
                isLowConfidence(top3), !ml.modelLoaded(),
                null, null, null, null, java.time.Instant.now());
    }

    public Page<ScanDto> history(User user, String disease, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long accountId = user.getAccount().getId();
        Page<Scan> scans = (disease == null || disease.isBlank())
                ? scanRepository.findByAccountId(accountId, pageable)
                : scanRepository.findByAccountIdAndPredictedDisease(accountId, disease, pageable);
        return scans.map(this::toListDto);
    }

    public ScanDto get(User user, Long id) {
        Scan scan = scanRepository.findById(id)
                .filter(s -> s.getAccount().getId().equals(user.getAccount().getId()))
                .orElseThrow(() -> ApiException.notFound("Scan not found"));
        Disease disease = diseaseRepository.findByDiseaseKey(scan.getPredictedDisease()).orElse(null);
        return toDto(scan, readTop3(scan), disease, true);
    }

    private MlClient.MlResult uploadToMl(MultipartFile image) {
        try {
            return mlClient.predict(image.getBytes(), image.getOriginalFilename(), image.getContentType());
        } catch (java.io.IOException e) {
            throw ApiException.badRequest("Could not read uploaded image");
        }
    }

    private Farm resolveFarm(User user, Long farmId) {
        if (farmId == null) return null;
        return farmRepository.findByIdAndAccountId(farmId, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Farm not found"));
    }

    private Crop resolveCrop(User user, Long cropId) {
        if (cropId == null) return null;
        return cropRepository.findByIdAndFarmAccountId(cropId, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Crop not found"));
    }

    private boolean isLowConfidence(List<PredictionDto> top3) {
        double top = top3.get(0).confidence();
        double second = top3.size() > 1 ? top3.get(1).confidence() : 0.0;
        return top < confidenceThreshold || (top - second) < 0.15;
    }

    private ScanDto toDto(Scan scan, List<PredictionDto> top3, Disease disease, boolean modelLoaded) {
        return new ScanDto(
                scan.getId(),
                scan.getImageUrl(),
                scan.getPredictedDisease(),
                scan.getConfidence(),
                top3,
                disease != null ? DiseaseDto.from(disease) : null,
                isLowConfidence(top3),
                !modelLoaded,
                scan.getFarm() != null ? scan.getFarm().getId() : null,
                scan.getCrop() != null ? scan.getCrop().getId() : null,
                scan.getLatitude(),
                scan.getLongitude(),
                scan.getCreatedAt());
    }

    private ScanDto toListDto(Scan scan) {
        Disease disease = diseaseRepository.findByDiseaseKey(scan.getPredictedDisease()).orElse(null);
        return toDto(scan, readTop3(scan), disease, true);
    }

    private String writeJson(List<PredictionDto> top3) {
        try {
            return objectMapper.writeValueAsString(top3);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<PredictionDto> readTop3(Scan scan) {
        try {
            if (scan.getTop3Json() == null) return List.of();
            return objectMapper.readValue(scan.getTop3Json(), new TypeReference<List<PredictionDto>>() {});
        } catch (Exception e) {
            return List.of(new PredictionDto(scan.getPredictedDisease(), scan.getConfidence()));
        }
    }
}
