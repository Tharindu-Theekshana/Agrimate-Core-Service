package com.agrimate.service.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplatesTest {

    // BE-TPL-01
    @Test
    void otpEmail_embedsTheCodeHeadingAndTtl() {
        String html = EmailTemplates.otpEmail("Verify your email", "Use this code.", "654321", 10);

        assertThat(html).contains("654321");
        assertThat(html).contains("Verify your email");
        assertThat(html).contains("10 minutes");
    }

    // BE-TPL-02
    @Test
    void otpEmail_escapesHtmlSpecialCharactersInTheHeading() {
        String html = EmailTemplates.otpEmail("<script>alert(1)</script>", "intro", "111111", 10);

        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains("&lt;script&gt;");
    }

    // BE-TPL-03
    @Test
    void adminInviteEmail_embedsBothTheUsernameAndTheTemporaryPassword() {
        String html = EmailTemplates.adminInviteEmail("newadmin", "482913");

        assertThat(html).contains("newadmin");
        assertThat(html).contains("482913");
    }

    // BE-TPL-04
    @Test
    void adminInviteEmail_escapesHtmlSpecialCharactersInTheUsername() {
        String html = EmailTemplates.adminInviteEmail("<b>admin</b>", "482913");

        assertThat(html).doesNotContain("<b>admin</b>");
    }

    // BE-TPL-05
    @Test
    void welcomeEmail_greetsTheAccountByName() {
        String html = EmailTemplates.welcomeEmail("Kasun Perera");

        assertThat(html).contains("Kasun Perera");
        assertThat(html).contains("AgriMate");
    }
}
