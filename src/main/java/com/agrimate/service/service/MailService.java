package com.agrimate.service.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSenderImpl mailSender;
    private final String from;
    private final boolean enabled;

    public MailService(JavaMailSenderImpl mailSender,
                       @Value("${spring.mail.username:}") String username,
                       @Value("${agrimate.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;

        boolean credentialsProvided = !username.isBlank();
        boolean connected = false;
        if (credentialsProvided) {
            try {
                mailSender.testConnection();
                connected = true;
                log.info("Mail (Brevo SMTP) connected successfully (username='{}').", username);
            } catch (Exception e) {
                log.warn("Mail credentials configured but the connection check failed (username='{}') — "
                        + "falling back to MOCK mode (emails are logged, not sent). Reason: {}", username, e.getMessage());
            }
        } else {
            log.warn("Mail (Brevo SMTP) not configured — running in MOCK mode (emails are logged, not sent). "
                    + "Set BREVO_SMTP_LOGIN / BREVO_SMTP_KEY to enable real sending.");
        }
        this.enabled = connected;
    }

    public void send(String to, String subject, String html) {
        if (!enabled) {
            log.info("Would send email to {}: \"{}\" ({} chars of HTML)", to, subject, html.length());
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}: \"{}\"", to, subject);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
