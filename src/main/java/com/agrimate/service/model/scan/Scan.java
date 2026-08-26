package com.agrimate.service.model.scan;

import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.farm.Farm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "scans", indexes = {
        @Index(name = "idx_scans_disease", columnList = "predicted_disease"),
        @Index(name = "idx_scans_account", columnList = "account_id")
})
public class Scan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "predicted_disease", nullable = false)
    private String predictedDisease;

    @Column(nullable = false)
    private Double confidence;

    @Lob
    @Column(name = "top3_json", columnDefinition = "TEXT")
    private String top3Json;

    private Double latitude;
    private Double longitude;

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPredictedDisease() { return predictedDisease; }
    public void setPredictedDisease(String predictedDisease) { this.predictedDisease = predictedDisease; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getTop3Json() { return top3Json; }
    public void setTop3Json(String top3Json) { this.top3Json = top3Json; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
