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
        String causeSi,
        String causeTa,
        String symptoms,
        String symptomsSi,
        String symptomsTa,
        String treatment,
        String treatmentSi,
        String treatmentTa,
        String prevention,
        String preventionSi,
        String preventionTa,
        Severity severity
) {
    public static DiseaseDto from(Disease d) {
        return new DiseaseDto(d.getDiseaseKey(), d.getNameEn(), d.getNameSi(), d.getNameTa(),
                d.getScientificName(),
                d.getCause(), d.getCauseSi(), d.getCauseTa(),
                d.getSymptoms(), d.getSymptomsSi(), d.getSymptomsTa(),
                d.getTreatment(), d.getTreatmentSi(), d.getTreatmentTa(),
                d.getPrevention(), d.getPreventionSi(), d.getPreventionTa(),
                d.getSeverity());
    }
}
