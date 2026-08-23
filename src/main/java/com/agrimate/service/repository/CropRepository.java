package com.agrimate.service.repository;

import com.agrimate.service.model.crop.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByFarmId(Long farmId);
    Optional<Crop> findByIdAndFarmAccountId(Long id, Long accountId);
}
