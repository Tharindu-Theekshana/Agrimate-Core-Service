package com.agrimate.service.service;

import com.agrimate.service.dto.AppVersionDtos.CheckResponse;
import com.agrimate.service.model.appVersion.AppVersion;
import com.agrimate.service.model.appVersion.Platform;
import com.agrimate.service.repository.AppVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppVersionServiceTest {

    @Mock private AppVersionRepository appVersionRepository;
    private AppVersionService appVersionService;

    @BeforeEach
    void setUp() {
        appVersionService = new AppVersionService(appVersionRepository);
    }

    private AppVersion latest(String version, boolean force) {
        AppVersion v = new AppVersion();
        v.setVersion(version);
        v.setPlatform(Platform.ANDROID);
        v.setForceUpdate(force);
        v.setLatest(true);
        return v;
    }

    // BE-VER-01
    @Test
    void check_reportsNoUpdate_whenNoVersionRowIsProvisionedForThePlatform() {
        when(appVersionRepository.findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(Platform.ANDROID))
                .thenReturn(Optional.empty());

        CheckResponse res = appVersionService.check(Platform.ANDROID, "1.0.0");

        assertThat(res.updateAvailable()).isFalse();
        assertThat(res.forceUpdate()).isFalse();
    }

    // BE-VER-02
    @Test
    void check_reportsUpdateAvailable_whenTheCurrentVersionIsOlder() {
        when(appVersionRepository.findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(Platform.ANDROID))
                .thenReturn(Optional.of(latest("2.0.0", false)));

        CheckResponse res = appVersionService.check(Platform.ANDROID, "1.0.0");

        assertThat(res.updateAvailable()).isTrue();
        assertThat(res.forceUpdate()).isFalse();
        assertThat(res.latestVersion()).isEqualTo("2.0.0");
    }

    // BE-VER-03
    @Test
    void check_reportsNoUpdate_whenAlreadyOnTheLatestVersion() {
        when(appVersionRepository.findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(Platform.ANDROID))
                .thenReturn(Optional.of(latest("1.0.0", false)));

        CheckResponse res = appVersionService.check(Platform.ANDROID, "1.0.0");

        assertThat(res.updateAvailable()).isFalse();
    }

    // BE-VER-04
    @Test
    void check_forceUpdateIsOnlyTrue_whenAnUpdateIsAlsoAvailable() {
        when(appVersionRepository.findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(Platform.ANDROID))
                .thenReturn(Optional.of(latest("1.0.0", true))); // force=true but already latest

        CheckResponse res = appVersionService.check(Platform.ANDROID, "1.0.0");

        assertThat(res.updateAvailable()).isFalse();
        assertThat(res.forceUpdate()).isFalse();
    }

    // BE-VER-05
    @Test
    void check_reportsForceUpdate_whenAnOlderVersionMustUpgrade() {
        when(appVersionRepository.findFirstByPlatformAndIsLatestTrueOrderByCreatedAtDesc(Platform.ANDROID))
                .thenReturn(Optional.of(latest("2.0.0", true)));

        CheckResponse res = appVersionService.check(Platform.ANDROID, "1.5.0");

        assertThat(res.updateAvailable()).isTrue();
        assertThat(res.forceUpdate()).isTrue();
    }

    // BE-VER-06..09
    @ParameterizedTest(name = "compare({0}, {1}) => {2}")
    @CsvSource({
            "1.9.0,  1.10.0, -1",   // numeric, not lexicographic, comparison
            "1.2.3,  1.2.3,   0",   // equal versions
            "1.2,    1.2.0,   0",   // missing trailing segments default to 0
            "2.0.0,  1.9.9,   1",   // newer major version wins regardless of minor/patch
    })
    void compare_performsNumericSemanticVersionComparison(String a, String b, int expectedSign) {
        int result = AppVersionService.compare(a, b);
        assertThat(Integer.signum(result)).isEqualTo(expectedSign);
    }
}
