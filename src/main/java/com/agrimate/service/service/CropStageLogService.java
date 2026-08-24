package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.CropStageLogDtos.CropStageLogDto;
import com.agrimate.service.dto.CropStageLogDtos.CropStageLogRequest;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.cropStageLog.CropStageLog;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.CropStageLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CropStageLogService {

    private final CropStageLogRepository logRepository;
    private final CropRepository cropRepository;

    public CropStageLogService(CropStageLogRepository logRepository, CropRepository cropRepository) {
        this.logRepository = logRepository;
        this.cropRepository = cropRepository;
    }

    public List<CropStageLogDto> list(User user, Long cropId) {
        ownedCrop(user, cropId);
        return logRepository.findByCropIdOrderByReachedDateAsc(cropId).stream().map(CropStageLogDto::from).toList();
    }

    @Transactional
    public CropStageLogDto create(User user, Long cropId, CropStageLogRequest req) {
        Crop crop = ownedCrop(user, cropId);
        CropStageLog log = new CropStageLog();
        log.setCrop(crop);
        log.setStageKey(req.stageKey());
        log.setReachedDate(req.reachedDate());
        CropStageLog saved = logRepository.save(log);
        syncCurrentStage(crop);
        return CropStageLogDto.from(saved);
    }

    @Transactional
    public void delete(User user, Long id) {
        CropStageLog log = logRepository.findByIdAndCropFarmAccountId(id, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Stage log not found"));
        Crop crop = log.getCrop();
        logRepository.delete(log);
        syncCurrentStage(crop);
    }

    private void syncCurrentStage(Crop crop) {
        List<CropStageLog> logs = logRepository.findByCropIdOrderByReachedDateAsc(crop.getId());
        String latest = logs.isEmpty() ? null : logs.get(logs.size() - 1).getStageKey();
        crop.setGrowthStage(latest);
        cropRepository.save(crop);
    }

    private Crop ownedCrop(User user, Long cropId) {
        return cropRepository.findByIdAndFarmAccountId(cropId, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Crop not found"));
    }
}
