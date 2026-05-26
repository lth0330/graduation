package app.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class FcmService {

    @PostConstruct
    public void init() {
        try {
            // 자바 프로젝트의 src/main/resources 폴더 안에 firebase-key.json을 넣어주세요!
            ClassPathResource resource = new ClassPathResource("firebase-key.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 알림 발송 공통 메서드
    public void sendPush(String token, String title, String body) {
        if (token == null || token.isEmpty()) return;
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("FCM 발송 에러: " + e.getMessage());
        }
    }
}