package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.TreatmentDtos.TreatmentDto;
import com.agrimate.service.dto.TreatmentDtos.TreatmentRequest;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.treatmentLog.TreatmentLog;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.treatmentLog.TreatmentType;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.TreatmentLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TreatmentService {

    private final TreatmentLogRepository treatmentRepository;
    private final CropRepository cropRepository;

    public TreatmentService(TreatmentLogRepository treatmentRepository, CropRepository cropRepository) {
        this.treatmentRepository = treatmentRepository;
        this.cropRepository = cropRepository;
    }

    public List<TreatmentDto> list(User user, Long cropId) {
        ownedCrop(user, cropId);
        return treatmentRepository.findByCropIdOrderByAppliedDateDesc(cropId).stream().map(TreatmentDto::from).toList();
    }

    @Transactional
    public TreatmentDto create(User user, Long cropId, TreatmentRequest req) {
        Crop crop = ownedCrop(user, cropId);
        TreatmentLog log = new TreatmentLog();
        log.setCrop(crop);
        log.setProductName(req.productName().trim());
        log.setType(req.type() != null ? req.type() : TreatmentType.FERTILIZER);
        log.setQuantity(req.quantity());
        log.setAppliedDate(req.appliedDate());
        return TreatmentDto.from(treatmentRepository.save(log));
    }

    @Transactional
    public void delete(User user, Long id) {
        TreatmentLog log = treatmentRepository.findByIdAndCropFarmAccountId(id, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Treatment log not found"));
        treatmentRepository.delete(log);
    }

    private Crop ownedCrop(User user, Long cropId) {
        return cropRepository.findByIdAndFarmAccountId(cropId, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Crop not found"));
    }
}
