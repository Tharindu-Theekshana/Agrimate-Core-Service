package com.agrimate.service.model.treatmentLog;

import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.baseEntity.BaseEntity;
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
@Table(name = "treatment_logs")
public class TreatmentLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TreatmentType type;

    private String quantity;

    @Column(name = "applied_date")
    private LocalDate appliedDate;

    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public TreatmentType getType() { return type; }
    public void setType(TreatmentType type) { this.type = type; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }
}
