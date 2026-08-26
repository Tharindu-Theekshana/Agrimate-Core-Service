package com.agrimate.service.model.disease;

import com.agrimate.service.model.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "diseases")
public class Disease extends BaseEntity {

    @Column(name = "disease_key", nullable = false, unique = true)
    private String diseaseKey;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_si")
    private String nameSi;

    @Column(name = "name_ta")
    private String nameTa;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(columnDefinition = "TEXT")
    private String cause;

    @Column(name = "cause_si", columnDefinition = "TEXT")
    private String causeSi;

    @Column(name = "cause_ta", columnDefinition = "TEXT")
    private String causeTa;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "symptoms_si", columnDefinition = "TEXT")
    private String symptomsSi;

    @Column(name = "symptoms_ta", columnDefinition = "TEXT")
    private String symptomsTa;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "treatment_si", columnDefinition = "TEXT")
    private String treatmentSi;

    @Column(name = "treatment_ta", columnDefinition = "TEXT")
    private String treatmentTa;

    @Column(columnDefinition = "TEXT")
    private String prevention;

    @Column(name = "prevention_si", columnDefinition = "TEXT")
    private String preventionSi;

    @Column(name = "prevention_ta", columnDefinition = "TEXT")
    private String preventionTa;

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.MEDIUM;

    public String getDiseaseKey() { return diseaseKey; }
    public void setDiseaseKey(String diseaseKey) { this.diseaseKey = diseaseKey; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameSi() { return nameSi; }
    public void setNameSi(String nameSi) { this.nameSi = nameSi; }
    public String getNameTa() { return nameTa; }
    public void setNameTa(String nameTa) { this.nameTa = nameTa; }
    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public String getCause() { return cause; }
    public void setCause(String cause) { this.cause = cause; }
    public String getCauseSi() { return causeSi; }
    public void setCauseSi(String causeSi) { this.causeSi = causeSi; }
    public String getCauseTa() { return causeTa; }
    public void setCauseTa(String causeTa) { this.causeTa = causeTa; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getSymptomsSi() { return symptomsSi; }
    public void setSymptomsSi(String symptomsSi) { this.symptomsSi = symptomsSi; }
    public String getSymptomsTa() { return symptomsTa; }
    public void setSymptomsTa(String symptomsTa) { this.symptomsTa = symptomsTa; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public String getTreatmentSi() { return treatmentSi; }
    public void setTreatmentSi(String treatmentSi) { this.treatmentSi = treatmentSi; }
    public String getTreatmentTa() { return treatmentTa; }
    public void setTreatmentTa(String treatmentTa) { this.treatmentTa = treatmentTa; }
    public String getPrevention() { return prevention; }
    public void setPrevention(String prevention) { this.prevention = prevention; }
    public String getPreventionSi() { return preventionSi; }
    public void setPreventionSi(String preventionSi) { this.preventionSi = preventionSi; }
    public String getPreventionTa() { return preventionTa; }
    public void setPreventionTa(String preventionTa) { this.preventionTa = preventionTa; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
}
