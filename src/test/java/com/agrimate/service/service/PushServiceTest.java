package com.agrimate.service.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;


class PushServiceTest {

    // BE-PUSH-01
    @Test
    void constructingWithoutCredentialsPath_fallsBackToMockModeWithoutAttemptingFirebaseInit() {
        assertThatCode(() -> new PushService("")).doesNotThrowAnyException();
    }

    // BE-PUSH-02
    @Test
    void sendToTokens_inMockMode_logsAndReturnsWithoutThrowing() {
        PushService pushService = new PushService("");

        assertThatCode(() -> pushService.sendToTokens(List.of("token-1", "token-2"), "Title", "Body", "SYSTEM"))
                .doesNotThrowAnyException();
    }

    // BE-PUSH-03
    @Test
    void sendToTokens_withNoTokens_returnsImmediately() {
        PushService pushService = new PushService("");

        assertThatCode(() -> pushService.sendToTokens(List.of(), "Title", "Body", "SYSTEM"))
                .doesNotThrowAnyException();
    }
}
