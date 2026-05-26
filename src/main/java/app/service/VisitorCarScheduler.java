package app.service;

import app.entity.RegisteredCarEntity;
import app.entity.AppNotificationEntity;
import app.repository.RegisteredCarRepository;
import app.repository.AppNotificationRepository;
import app.repository.DeviceInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorCarScheduler {

    private final RegisteredCarRepository registeredCarRepository;
    private final AppNotificationRepository notificationRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final FcmService fcmService;
    private final app.repository.AppSettingRepository settingRepository;


    // 매 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkVisitorCarExpiration() {
        LocalDateTime now = LocalDateTime.now();
        List<RegisteredCarEntity> visitorCars = registeredCarRepository.findAll();

        for (RegisteredCarEntity car : visitorCars) {
            if (car.getExpiresAt() == null) continue;

            long remainMins = ChronoUnit.MINUTES.between(now, car.getExpiresAt());

            // 1. 임박 알림 (3시간, 2시간, 1시간, 30분, 10분, 5분 전)
            if (remainMins == 180 || remainMins == 120 || remainMins == 60 || remainMins == 30 || remainMins == 10 || remainMins == 5) {
                String timeText = remainMins >= 60 ? (remainMins / 60) + "시간" : remainMins + "분";
                String title = "⏰ 방문 차량 만료 임박";
                String body = "등록하신 방문 차량(" + car.getNumber() + ")의 주차 시간이 " + timeText + " 남았습니다.";
                
                sendNotificationAndPush(car, title, body);
            }
            
            // 2. 만료 시 삭제 및 알림
            else if (remainMins <= 0) {
                String body = "방문 차량(" + car.getNumber() + ")의 24시간 주차가 만료되어 자동 출차 처리되었습니다.";
                sendNotificationAndPush(car, "⏰ 방문 차량 만료 알림", body);
                
                registeredCarRepository.delete(car);
            }
        }
    }

private void sendNotificationAndPush(RegisteredCarEntity car, String title, String body) {
        // DB 알림 보관함 저장 (무조건 저장)
        notificationRepository.save(AppNotificationEntity.builder()
                .resident(car.getResident())
                .type("visitor")
                .title(title)
                .message(body)
                .read(false)
                .build());

        // 👇 FCM 푸시 발송 (설정이 ON일 때만 폰으로 발송)
        Integer residentNo = car.getResident().getNo();
        boolean isPushOn = settingRepository.findByDeviceId("device_" + residentNo)
                .map(app.entity.AppSettingEntity::getAlertPush)
                .orElse(true);

        if (isPushOn) {
            deviceInfoRepository.findByResident_No(residentNo).forEach(device -> {
                fcmService.sendPush(device.getFcmToken(), title, body);
            });
        }
    }