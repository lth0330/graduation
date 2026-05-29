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
import lombok.RequiredArgsConstructor; // 💡 추가
import app.repository.DeviceInfoRepository; // 💡 추가
import com.google.firebase.messaging.FirebaseMessagingException; // 💡 이 줄이 꼭 있어야 합니다!
import com.google.firebase.messaging.MessagingErrorCode;

@Service
@RequiredArgsConstructor // 💡 생성자 주입을 위해 추가
public class FcmService {

    private final DeviceInfoRepository deviceInfoRepository; // 💡 리포지토리 주입

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

    public void sendPush(String token, String title, String body) {
        if (token == null || token.isEmpty()) return;
        System.out.println("DEBUG: 푸시 발송 시도 - 토큰 길이: " + token.length() + ", 토큰 값: " + token);
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            if (MessagingErrorCode.UNREGISTERED.equals(errorCode) ||
                    MessagingErrorCode.INVALID_ARGUMENT.equals(errorCode)) {
                System.err.println("❌ 유효하지 않은 토큰 발견, 삭제 처리합니다: " + token);
                deviceInfoRepository.deleteByFcmToken(token);
            } else {
                System.err.println("⚠️ FCM 발송 에러 (코드: " + errorCode + "): " + e.getMessage());
            }
            // 💡 [여기가 추가되어야 합니다] FCM 외의 예상치 못한 모든 에러를 막아주는 최후의 방어막!
        } catch (Exception e) {
            System.err.println("⚠️ 일반 푸시 발송 에러: " + e.getMessage());
        }
    }
}
