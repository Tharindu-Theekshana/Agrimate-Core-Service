package com.agrimate.service.repository;

import com.agrimate.service.model.disease.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    Optional<Disease> findByDiseaseKey(String diseaseKey);
    boolean existsByDiseaseKey(String diseaseKey);
}
