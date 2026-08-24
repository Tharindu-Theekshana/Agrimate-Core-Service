package com.agrimate.service.model.cropStageLog;

import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.crop.Crop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "crop_stage_logs")
public class CropStageLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(name = "stage_key", nullable = false)
    private String stageKey;

    @Column(name = "reached_date", nullable = false)
    private LocalDate reachedDate;

    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public String getStageKey() { return stageKey; }
    public void setStageKey(String stageKey) { this.stageKey = stageKey; }
    public LocalDate getReachedDate() { return reachedDate; }
    public void setReachedDate(LocalDate reachedDate) { this.reachedDate = reachedDate; }
}
