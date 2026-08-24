package com.agrimate.service.repository;

import com.agrimate.service.model.cropStageLog.CropStageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropStageLogRepository extends JpaRepository<CropStageLog, Long> {
    List<CropStageLog> findByCropIdOrderByReachedDateAsc(Long cropId);
    Optional<CropStageLog> findByIdAndCropFarmAccountId(Long id, Long accountId);
}
