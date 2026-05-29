package app.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import app.dto.AppDeviceTokenRequestDto;
import app.dto.AppInquiryCreateRequestDto;
import app.dto.AppParkingUpdateItemDto;
import app.dto.AppParkingUpdateRequestDto;
import app.dto.AppSettingRequestDto;
import app.dto.AppVisitorEntryRequestDto;
import app.dto.AppWaitlistRequestDto;
import app.entity.AppNotificationEntity;
import app.entity.AppSettingEntity;
import app.entity.DeviceInfoEntity;
import app.entity.RegisteredCarEntity;
import app.entity.WaitingListEntity;
import app.repository.AppNotificationRepository;
import app.repository.AppSettingRepository;
import app.repository.DeviceInfoRepository;
import app.repository.RegisteredCarRepository;
import app.repository.WaitingListRepository;
import web.inquiry.entity.ResidentInquiryEntity;
import web.inquiry.repository.ResidentInquiryRepository;
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 앱 입주민 기능 서비스: 문의, 알림, 설정, 방문차량 입차, 주차 상태 업데이트 CRUD를 처리한다.
public class AppResidentFeatureService {

    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ResidentInquiryRepository residentInquiryRepository;
    private final AppNotificationRepository notificationRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final AppSettingRepository settingRepository;
    private final WaitingListRepository waitingListRepository;
    private final RegisteredCarRepository registeredCarRepository;
    private final ParkingZoneRepository parkingZoneRepository;
    private final FcmService fcmService;
    private final ManagerNotificationService managerNotificationService;

    public Map<String, Object> findInquiries(Integer residentNo) {
        Map<String, Object> response = success();
        response.put("inquiries", residentInquiryRepository.findByResident_NoOrderByCreatedAtDesc(residentNo)
                .stream()
                .map(this::toInquiryMap)
                .toList());
        return response;
    }

    @Transactional
    public Map<String, Object> createInquiry(Integer residentNo, AppInquiryCreateRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getTitle()) || isBlank(requestDto.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inquiry title and content are required.");
        }

        ResidentEntity resident = findResident(residentNo);
        ResidentVehicleEntity vehicle = resolveInquiryVehicle(residentNo, requestDto.getCarNo());

        ResidentInquiryEntity inquiry = ResidentInquiryEntity.builder()
                .resident(resident)
                .vehicle(vehicle)
                .title(requestDto.getTitle().trim())
                .content(requestDto.getContent().trim())
                .status("pending")
                .build();
        ResidentInquiryEntity savedInquiry = residentInquiryRepository.save(inquiry);
        managerNotificationService.createApartmentNotification(
                resident.getApartment(),
                "resident_inquiry",
                "새 입주민 문의",
                resident.getName() + " 입주민이 새로운 문의를 등록했습니다.",
                "resident_inquiry",
                savedInquiry.getNo()
        );
        return success();
    }

    public Map<String, Object> findNotifications(Integer residentNo) {
        Map<String, Object> response = success();
        response.put("notifications", notificationRepository.findByResident_NoOrderByCreatedAtDesc(residentNo)
                .stream()
                .map(this::toNotificationMap)
                .toList());
        return response;
    }
    // AppResidentFeatureService.java 파일 내부 아무 곳에나 추가하세요.
    @Transactional
    public Map<String, Object> sendTestPush(Integer residentNo) {
        // 1. 해당 유저의 기기 토큰을 찾습니다.
        deviceInfoRepository.findByResident_No(residentNo).forEach(device -> {
            // 2. FcmService를 통해 알림을 보냅니다.
            fcmService.sendPush(device.getFcmToken(), "🚀 테스트 푸시 알림", "지금 서버에서 푸시 알림이 정상적으로 전송되었습니다!");
        });
        return success();
    }
    @Transactional
    public Map<String, Object> readNotification(Integer residentNo, Integer notificationNo) {
        AppNotificationEntity notification = notificationRepository.findById(notificationNo)
                .filter(item -> item.getResident().getNo().equals(residentNo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        notification.setRead(true);
        return success();
    }

    @Transactional
    public Map<String, Object> saveDeviceToken(Integer residentNo, AppDeviceTokenRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getFcmToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FCM token is required.");
        }

        ResidentEntity resident = findResident(residentNo);
        String deviceId = deviceId(residentNo);
        // 현재 앱은 별도 기기 ID가 없어서 입주민 번호 기반의 고정 deviceId를 사용한다.
        DeviceInfoEntity deviceInfo = deviceInfoRepository.findById(deviceId)
                .orElse(DeviceInfoEntity.builder()
                        .deviceId(deviceId)
                        .resident(resident)
                        .build());
        deviceInfo.setFcmToken(requestDto.getFcmToken());
        deviceInfo.setLastLoginAt(LocalDateTime.now());
        deviceInfoRepository.save(deviceInfo);
        return success();
    }

    @Transactional
    public Map<String, Object> deleteDeviceToken(Integer residentNo) {
        deviceInfoRepository.findById(deviceId(residentNo)).ifPresent(deviceInfo -> deviceInfo.setFcmToken(null));
        return success();
    }

    @Transactional
    public Map<String, Object> updatePushSetting(Integer residentNo, AppSettingRequestDto requestDto) {
        AppSettingEntity setting = findOrCreateSetting(residentNo);
        // 앱 설정은 device_info와 1:1로 연결되므로 없으면 먼저 생성한다.
        setting.setAlertPush(requestDto != null ? requestDto.getAlertPush() : null);
        settingRepository.save(setting);
        return success();
    }

    @Transactional
    public Map<String, Object> updateThemeSetting(Integer residentNo, AppSettingRequestDto requestDto) {
        AppSettingEntity setting = findOrCreateSetting(residentNo);
        setting.setThemeMode(requestDto != null && !isBlank(requestDto.getThemeMode()) ? requestDto.getThemeMode() : "light");
        settingRepository.save(setting);
        return success();
    }

    @Transactional
    public Map<String, Object> createWaitlist(Integer residentNo, AppWaitlistRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getTargetSlotId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target slot id is required.");
        }

        waitingListRepository.save(WaitingListEntity.builder()
                .resident(findResident(residentNo))
                .targetSlotId(requestDto.getTargetSlotId())
                .build());
        return success();
    }

    @Transactional
    public Map<String, Object> visitorEntry(AppVisitorEntryRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getCarNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car number is required.");
        }

        // 방문 차량 입차 시 parkedAt/expireAt을 기록하고 입주민 알림을 남긴다.
        RegisteredCarEntity registeredCar = registeredCarRepository
                .findFirstByNumberAndParkedAtIsNull(requestDto.getCarNumber().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registered visitor car not found."));

        registeredCar.setParkedAt(LocalDateTime.now());
        registeredCar.setExpiresAt(LocalDateTime.now().plusDays(1));

        notificationRepository.save(AppNotificationEntity.builder()
                .resident(registeredCar.getResident())
                .type("visitor")
                .title("\uBC29\uBB38 \uCC28\uB7C9 \uC785\uCC28 \uC54C\uB9BC")
                .message("[" + registeredCar.getNumber() + "] \uBC29\uBB38 \uCC28\uB7C9\uC774 \uC8FC\uCC28\uC7A5\uC5D0 \uB4E4\uC5B4\uC654\uC2B5\uB2C8\uB2E4.")
                .build());

        return success();
    }

    @Transactional
    public Map<String, Object> updateParking(AppParkingUpdateRequestDto requestDto) {
        if (requestDto == null || requestDto.getUpdates() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parking updates are required.");
        }

        for (AppParkingUpdateItemDto update : requestDto.getUpdates()) {
            if (update == null || isBlank(update.getSlot()) || isBlank(update.getStatus())) continue;

            parkingZoneRepository.findByAreaNumber(update.getSlot()).ifPresent(zone -> {
                zone.setStatus(update.getStatus());

                // 💡 1. 빈자리가 났을 때 대기자에게 알림 발송
                if (update.getStatus().equals("empty")) {
                    waitingListRepository.findAll().stream()
                            .filter(w -> !w.getNotified() && (w.getTargetSlotId().equals("ALL") || w.getTargetSlotId().equals(update.getSlot())))
                            .forEach(w -> {
                                String msg = "대기하시던 [" + update.getSlot() + "] 구역에 빈자리가 생겼습니다! 먼저 주차하세요.";
                                w.setNotified(true);

                                notificationRepository.save(AppNotificationEntity.builder()
                                        .resident(w.getResident()).type("system").title("🔔 빈자리 알림").message(msg).read(false).build());

                                boolean isPushOn = settingRepository.findByDeviceId("device_" + w.getResident().getNo())
                                        .map(AppSettingEntity::getAlertPush).orElse(true);
                                if (isPushOn) {
                                    deviceInfoRepository.findByResident_No(w.getResident().getNo())
                                            .forEach(d -> fcmService.sendPush(d.getFcmToken(), "🔔 빈자리 알림", msg));
                                }
                            });
                }
                // 💡 2. 주차 완료 시 차주에게 알림 발송 (차량 번호가 전달된 경우)
                else if ((update.getStatus().equals("occupied") || update.getStatus().equals("사용중")) && zone.getCurrentCarNumber() != null) {
                    residentVehicleRepository.findByNumber(zone.getCurrentCarNumber()).ifPresent(car -> {
                        String msg = "[" + update.getSlot() + "] 구역에 차량(" + car.getNumber() + ") 주차가 완료되었습니다.";
                        notificationRepository.save(AppNotificationEntity.builder()
                                .resident(car.getResident()).type("system").title("🅿️ 주차 완료 알림").message(msg).read(false).build());

// 👇 [이렇게 변경해 주세요] 👇
                        boolean isPushOn2 = settingRepository.findByDeviceId("device_" + car.getResident().getNo())
                                .map(AppSettingEntity::getAlertPush).orElse(true);
                        if (isPushOn2) {
                            deviceInfoRepository.findByResident_No(car.getResident().getNo())
                                    .forEach(d -> fcmService.sendPush(d.getFcmToken(), "🅿️ 주차 완료 알림", msg));
                        }
                    });
                }
            });
        }
        return success();
    }
    private AppSettingEntity findOrCreateSetting(Integer residentNo) {
        String deviceId = deviceId(residentNo);
        ensureDeviceInfo(residentNo);
        // 설정 데이터가 없으면 앱 기본값으로 새로 만든다.
        return settingRepository.findByDeviceId(deviceId)
                .orElse(AppSettingEntity.builder()
                        .deviceId(deviceId)
                        .alertPush(true)
                        .themeMode("light")
                        .build());
    }

    private void ensureDeviceInfo(Integer residentNo) {
        String deviceId = deviceId(residentNo);
        if (deviceInfoRepository.existsById(deviceId)) {
            return;
        }
        deviceInfoRepository.save(DeviceInfoEntity.builder()
                .deviceId(deviceId)
                .resident(findResident(residentNo))
                .build());
    }

    private ResidentEntity findResident(Integer residentNo) {
        return residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));
    }

    private ResidentVehicleEntity resolveInquiryVehicle(Integer residentNo, Integer vehicleNo) {
        if (vehicleNo != null) {
            ResidentVehicleEntity vehicle = residentVehicleRepository.findById(vehicleNo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found."));
            if (vehicle.getResident() == null || !vehicle.getResident().getNo().equals(residentNo)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resident and car do not match.");
            }
            return vehicle;
        }

        return residentVehicleRepository.findByResident_No(residentNo)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> toInquiryMap(ResidentInquiryEntity inquiry) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("inquiry_no", inquiry.getNo());
        item.put("u_no", inquiry.getResident() != null ? inquiry.getResident().getNo() : null);
        item.put("c_no", inquiry.getVehicle() != null ? inquiry.getVehicle().getNo() : null);
        item.put("title", inquiry.getTitle());
        item.put("content", inquiry.getContent());
        item.put("status", inquiry.getStatus());
        item.put("answer", inquiry.getAnswer());
        item.put("created_at", inquiry.getCreatedAt());
        item.put("answered_at", inquiry.getAnsweredAt());
        return item;
    }

    private Map<String, Object> toNotificationMap(AppNotificationEntity notification) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("noti_no", notification.getNo());
        item.put("u_no", notification.getResident() != null ? notification.getResident().getNo() : null);
        item.put("noti_type", notification.getType());
        item.put("noti_title", notification.getTitle());
        item.put("noti_message", notification.getMessage());
        item.put("is_read", Boolean.TRUE.equals(notification.getRead()) ? 1 : 0);
        item.put("created_at", notification.getCreatedAt());
        return item;
    }

    private String deviceId(Integer residentNo) {
        return "device_" + residentNo;
    }

    private Map<String, Object> success() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
