package com.agrimate.service.service;

import com.agrimate.service.dto.AppVersionDtos.CheckResponse;
import com.agrimate.service.model.appVersion.AppVersion;
import com.agrimate.service.model.appVersion.Platform;
import com.agrimate.service.repository.AppVersionRepository;
import org.springframework.stereotype.Service;

@Service
public class AppVersionService {

    private final AppVersionRepository appVersionRepository;

    public AppVersionService(AppVersionRepository appVersionRepository) {
        this.appVersionRepository = appVersionRepository;
    }

    public CheckResponse check(Platform platform, String currentVersion) {
        AppVersion latest = appVersionRepository
                .findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(platform)
                .orElse(null);
        if (latest == null) {
            return new CheckResponse(false, false, currentVersion, currentVersion);
        }
        boolean updateAvailable = compare(currentVersion, latest.getVersion()) < 0;
        boolean force = updateAvailable && latest.isForceUpdate();
        return new CheckResponse(updateAvailable, force, latest.getVersion(), currentVersion);
    }

    static int compare(String a, String b) {
        String[] pa = safe(a).split("\\.");
        String[] pb = safe(b).split("\\.");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int va = i < pa.length ? parse(pa[i]) : 0;
            int vb = i < pb.length ? parse(pb[i]) : 0;
            if (va != vb) return Integer.compare(va, vb);
        }
        return 0;
    }

    private static String safe(String v) {
        return v == null ? "0" : v.trim();
    }

    private static int parse(String s) {
        StringBuilder digits = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else break;
        }
        return digits.isEmpty() ? 0 : Integer.parseInt(digits.toString());
    }
}
