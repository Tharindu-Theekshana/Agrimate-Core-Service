package com.agrimate.service.model.farm;

import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "farms")
public class Farm extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private String name;

    private Double latitude;
    private Double longitude;

    @Column(name = "size_acres")
    private Double sizeAcres;

    @Column(name = "soil_type")
    private String soilType;

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getSizeAcres() { return sizeAcres; }
    public void setSizeAcres(Double sizeAcres) { this.sizeAcres = sizeAcres; }
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
}
