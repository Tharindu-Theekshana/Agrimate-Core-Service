package com.agrimate.service.model.account;

import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_accounts_phone", columnList = "phone", unique = true)
})
public class Account extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private RoleName accountType;

    @Column(unique = true)
    private String phone;

    private String location;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "agronomist_status")
    private AgronomistStatus agronomistStatus = AgronomistStatus.NONE;

    @Column(nullable = false)
    private boolean suspended = false;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RoleName getAccountType() { return accountType; }
    public void setAccountType(RoleName accountType) { this.accountType = accountType; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public AgronomistStatus getAgronomistStatus() { return agronomistStatus; }
    public void setAgronomistStatus(AgronomistStatus agronomistStatus) { this.agronomistStatus = agronomistStatus; }
    public boolean isSuspended() { return suspended; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }
}
