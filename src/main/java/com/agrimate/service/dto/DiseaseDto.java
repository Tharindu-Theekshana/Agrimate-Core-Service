package com.agrimate.service.dto;

import com.agrimate.service.model.disease.Disease;
import com.agrimate.service.model.disease.Severity;

public record DiseaseDto(
        String diseaseKey,
        String nameEn,
        String nameSi,
        String nameTa,
        String scientificName,
        String cause,
        String symptoms,
        String treatment,
        String prevention,
        Severity severity
) {
    public static DiseaseDto from(Disease d) {
        return new DiseaseDto(d.getDiseaseKey(), d.getNameEn(), d.getNameSi(), d.getNameTa(),
                d.getScientificName(), d.getCause(), d.getSymptoms(), d.getTreatment(),
                d.getPrevention(), d.getSeverity());
    }
}
