package com.agrimate.service.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.List;

@Service
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final boolean enabled;

    public PushService(@Value("${agrimate.firebase.credentials-path:}") String credentialsPath) {
        this.enabled = init(credentialsPath);
        if (!enabled) {
            log.warn("Firebase not configured — running in MOCK push mode (notifications are still saved to the "
                    + "in-app inbox, but no OS-level push is sent). Set agrimate.firebase.credentials-path to a "
                    + "service-account JSON file to enable real pushes.");
        }
    }

    private boolean init(String credentialsPath) {
        if (credentialsPath == null || credentialsPath.isBlank()) return false;
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            return true;
        } catch (Exception e) {
            log.warn("Failed to initialize Firebase from '{}' — falling back to MOCK push mode: {}",
                    credentialsPath, e.getMessage());
            return false;
        }
    }

    public void sendToTokens(List<String> tokens, String title, String body, String type) {
        if (tokens.isEmpty()) return;
        if (!enabled) {
            log.info("Would send to {} device(s): \"{}\" — {}", tokens.size(), title, body);
            return;
        }
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", type)
                    .addAllTokens(tokens)
                    .build();
            var response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Push sent: {} succeeded, {} failed (of {})",
                    response.getSuccessCount(), response.getFailureCount(), tokens.size());
        } catch (Exception e) {
            log.warn("Failed to send push notification: {}", e.getMessage());
        }
    }
}
