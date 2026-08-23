package com.agrimate.service.model.crop;

import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.farm.Farm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "crops")
public class Crop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "crop_type", nullable = false)
    private String cropType = "paddy";

    private String variety;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Column(name = "area_acres")
    private Double areaAcres;

    @Column(name = "planting_date")
    private LocalDate plantingDate;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @Column(name = "growth_stage")
    private String growthStage;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.ColumnDefault("'GROWING'")
    @Column(nullable = false)
    private CropStatus status = CropStatus.GROWING;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "yield_kg")
    private Double yieldKg;

    @Column(name = "quality_grade")
    private String qualityGrade;

    @Column(name = "selling_price")
    private Double sellingPrice;

    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }
    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }
    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
    public Double getAreaAcres() { return areaAcres; }
    public void setAreaAcres(Double areaAcres) { this.areaAcres = areaAcres; }
    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }
    public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
    public void setExpectedHarvestDate(LocalDate d) { this.expectedHarvestDate = d; }
    public String getGrowthStage() { return growthStage; }
    public void setGrowthStage(String growthStage) { this.growthStage = growthStage; }
    public CropStatus getStatus() { return status; }
    public void setStatus(CropStatus status) { this.status = status; }
    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }
    public Double getYieldKg() { return yieldKg; }
    public void setYieldKg(Double yieldKg) { this.yieldKg = yieldKg; }
    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
}
