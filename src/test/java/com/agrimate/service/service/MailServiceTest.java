package com.agrimate.service.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThatCode;


class MailServiceTest {

    // BE-MAIL-01
    @Test
    void constructingWithoutCredentials_fallsBackToMockModeWithoutAttemptingAConnection() {
        assertThatCode(() -> new MailService(new JavaMailSenderImpl(), "", "agrimate@example.com"))
                .doesNotThrowAnyException();
    }

    // BE-MAIL-02
    @Test
    void send_inMockMode_logsAndReturnsWithoutThrowing() {
        MailService mailService = new MailService(new JavaMailSenderImpl(), "", "agrimate@example.com");

        assertThatCode(() -> mailService.send("farmer@agrimate.lk", "Subject", "<p>Body</p>"))
                .doesNotThrowAnyException();
    }
}
