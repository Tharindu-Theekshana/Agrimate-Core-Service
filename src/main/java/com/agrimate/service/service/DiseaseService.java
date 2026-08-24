package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.DiseaseDto;
import com.agrimate.service.model.disease.Disease;
import com.agrimate.service.repository.DiseaseRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    public DiseaseService(DiseaseRepository diseaseRepository) {
        this.diseaseRepository = diseaseRepository;
    }

    public List<DiseaseDto> list() {
        return diseaseRepository.findAll().stream()
                .sorted(Comparator.comparing(Disease::getNameEn))
                .map(DiseaseDto::from)
                .toList();
    }

    public DiseaseDto get(String key) {
        return DiseaseDto.from(find(key));
    }

    public Disease find(String key) {
        return diseaseRepository.findByDiseaseKey(key)
                .orElseThrow(() -> ApiException.notFound("Disease not found: " + key));
    }
}
