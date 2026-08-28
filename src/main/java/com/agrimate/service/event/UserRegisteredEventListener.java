package com.agrimate.service.event;

import com.agrimate.service.service.EmailTemplates;
import com.agrimate.service.service.MailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisteredEventListener {

    private final MailService mailService;

    public UserRegisteredEventListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        String html = EmailTemplates.welcomeEmail(event.name());
        mailService.send(event.email(), "Welcome to AgriMate!", html);
    }
}
