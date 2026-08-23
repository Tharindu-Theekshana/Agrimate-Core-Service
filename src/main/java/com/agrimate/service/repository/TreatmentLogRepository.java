package com.agrimate.service.repository;

import com.agrimate.service.model.treatmentLog.TreatmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentLogRepository extends JpaRepository<TreatmentLog, Long> {
    List<TreatmentLog> findByCropIdOrderByAppliedDateDesc(Long cropId);
    Optional<TreatmentLog> findByIdAndCropFarmAccountId(Long id, Long accountId);
}
