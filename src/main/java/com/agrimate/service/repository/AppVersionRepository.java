package com.agrimate.service.repository;

import com.agrimate.service.model.appVersion.AppVersion;
import com.agrimate.service.model.appVersion.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    Optional<AppVersion> findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(Platform platform);
}
