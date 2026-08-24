package com.agrimate.service.model.appVersion;

import com.agrimate.service.model.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_versions", indexes = {
        @Index(name = "idx_app_versions_platform", columnList = "platform")
})
public class AppVersion extends BaseEntity {

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate = false;

    @Column(name = "is_latest", nullable = false)
    private boolean isLatest = false;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public boolean isForceUpdate() { return forceUpdate; }
    public void setForceUpdate(boolean forceUpdate) { this.forceUpdate = forceUpdate; }
    public boolean isLatest() { return isLatest; }
    public void setLatest(boolean latest) { isLatest = latest; }
}
