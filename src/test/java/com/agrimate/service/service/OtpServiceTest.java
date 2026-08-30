package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.otp.OtpPurpose;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OtpServiceTest {

    private OtpService defaultService() {
        return new OtpService(10, 15);
    }

    @Test
    void issue_returnsSixDigitNumericCode() {
        String code = defaultService().issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);
        assertThat(code).matches("\\d{6}");
    }

    @Test
    void verify_succeedsWithCorrectCode_andConsumesItSoItCannotBeReused() {
        OtpService service = defaultService();
        String code = service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);

        service.verify("farmer@agrimate.lk", OtpPurpose.REGISTRATION, code);

        assertThatThrownBy(() -> service.verify("farmer@agrimate.lk", OtpPurpose.REGISTRATION, code))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid or expired code");
    }

    @Test
    void verify_throwsBadRequest_whenCodeIsWrong() {
        OtpService service = defaultService();
        service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);

        assertThatThrownBy(() -> service.verify("farmer@agrimate.lk", OtpPurpose.REGISTRATION, "000000"))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @Test
    void verify_throwsBadRequest_whenNoCodeWasEverIssued() {
        OtpService service = defaultService();
        assertThatThrownBy(() -> service.verify("nobody@agrimate.lk", OtpPurpose.REGISTRATION, "123456"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void issue_throwsBadRequest_whenCalledAgainWithinResendCooldown() {
        OtpService service = defaultService();
        service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);

        assertThatThrownBy(() -> service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("wait a moment");
    }

    @Test
    void issue_succeedsAgain_onceCooldownHasElapsed() {
        OtpService service = new OtpService(10, 0); // zero-second cooldown
        String first = service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);
        String second = service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);

        assertThat(second).matches("\\d{6}");
        assertThatThrownBy(() -> service.verify("farmer@agrimate.lk", OtpPurpose.REGISTRATION, first))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void verify_throwsBadRequest_whenCodeHasExpired() throws InterruptedException {
        OtpService service = new OtpService(0, 0); // ttl=0 minutes -> expires almost immediately
        String code = service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);
        Thread.sleep(5);

        assertThatThrownBy(() -> service.verify("farmer@agrimate.lk", OtpPurpose.REGISTRATION, code))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid or expired code");
    }

    @Test
    void codesAreScopedByPurpose_registrationCodeDoesNotVerifyForPasswordReset() {
        OtpService service = defaultService();
        String code = service.issue("farmer@agrimate.lk", OtpPurpose.REGISTRATION);

        assertThatThrownBy(() -> service.verify("farmer@agrimate.lk", OtpPurpose.PASSWORD_RESET, code))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void emailMatchingIsCaseInsensitive() {
        OtpService service = defaultService();
        String code = service.issue("Farmer@AgriMate.LK", OtpPurpose.REGISTRATION);

        service.verify("farmer@agrimate.lk", OtpPurpose.REGISTRATION, code); // no exception
    }
}
