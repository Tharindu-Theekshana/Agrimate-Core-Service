package com.agrimate.service.dto;

public final class AppVersionDtos {
    private AppVersionDtos() {}

    public record CheckResponse(
            boolean updateAvailable,
            boolean forceUpdate,
            String latestVersion,
            String currentVersion
    ) {}
}
