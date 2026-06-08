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
import java.util.Comparator;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy; // 👈 맨 위 import 모여있는 곳에 추가
import org.springframework.context.annotation.Lazy; // 👈 import 추가
import org.springframework.beans.factory.annotation.Autowired; // 👈 import 추가


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
    // 👇 [새로 추가] 3분 뒤에 예약을 취소시킬 타이머 도구입니다.
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

// 👇 [추가] 자기 자신의 복제본(Proxy)을 불러와 트랜잭션을 유지하는 마법의 키워드!
    @Autowired
    @Lazy
    private AppResidentFeatureService self;

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
    // 👇👇 [새로 추가] 입주민이 알림 대기를 취소할 때 사용하는 기능! 👇👇
    @Transactional
    public Map<String, Object> cancelWaitlist(Integer residentNo) {
        // 해당 입주민이 걸어둔 모든 대기열을 찾아내서 깔끔하게 파기합니다.
        waitingListRepository.findAll().stream()
                .filter(w -> w.getResident().getNo().equals(residentNo))
                .forEach(waitingListRepository::delete);
        return success();
    }

    @Transactional
    public Map<String, Object> visitorEntry(AppVisitorEntryRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getCarNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car number is required.");
        }

        RegisteredCarEntity registeredCar = registeredCarRepository
                .findFirstByNumberAndParkedAtIsNull(requestDto.getCarNumber().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registered visitor car not found."));

        registeredCar.setParkedAt(LocalDateTime.now());
        registeredCar.setExpiresAt(LocalDateTime.now().plusDays(1));

        // 💡 푸시 알림과 DB 저장에 똑같이 쓸 문구를 변수로 만듭니다. (회원님의 유니코드 그대로 유지)
        String title = "\uBC29\uBB38 \uCC28\uB7C9 \uC785\uCC28 \uC54C\uB9BC";
        String message = "[" + registeredCar.getNumber() + "] \uBC29\uBB38 \uCC28\uB7C9\uC774 \uC8FC\uCC28\uC7A5\uC5D0 \uB4E4\uC5B4\uC654\uC2B5\uB2C8\uB2E4.";

        // 1. 기존 코드: 앱 내부 알림함(DB)에 저장
        notificationRepository.save(AppNotificationEntity.builder()
                .resident(registeredCar.getResident())
                .type("visitor")
                .title(title)
                .message(message)
                .build());

        // =========================================================
        // 2. 새로 추가된 코드: 스마트폰으로 푸시 알림(FCM) 발송
        // =========================================================
        Integer residentNo = registeredCar.getResident().getNo();
        boolean isPushOn = settingRepository.findByDeviceId("device_" + residentNo)
                .map(AppSettingEntity::getAlertPush)
                .orElse(true);

        if (isPushOn) {
            deviceInfoRepository.findByResident_No(residentNo).forEach(device -> {
                // 스마트폰 팝업창에는 자동차 이모티콘이 예쁘게 뜨도록 "🚗 "를 살짝 붙여줍니다.
                fcmService.sendPush(device.getFcmToken(), "🚗 " + title, message);
            });
        }
        // =========================================================

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

                // =========================================================
                // 💡 [핵심 방어 1] 카메라가 계속 'empty'를 보내도, 서버가 'reserved' 상태라면 철벽 방어!
                // =========================================================
                if (update.getStatus().equals("empty") && "reserved".equals(zone.getStatus())) {
                    return; // 아무 일도 하지 않고 무시 (3분 타이머가 끝날 때까지 보호)
                }

                zone.setStatus(update.getStatus());

                // =========================================================
                // 💡 [선착순 알림 로직] 빈자리가 났을 때
                // =========================================================
                if (update.getStatus().equals("empty")) {
                    // 모든 사람이 아니라, 대기열 중 '가장 먼저 신청한 1명'만 뽑아냅니다.
                    Optional<WaitingListEntity> firstWaiter = waitingListRepository.findAll().stream()
                            .filter(w -> !w.getNotified() && (w.getTargetSlotId().equals("ALL") || w.getTargetSlotId().equals(update.getSlot())))
                            .sorted(Comparator.comparing(WaitingListEntity::getNo)) // 번호가 작을수록 먼저 신청한 사람
                            .findFirst();

                    if (firstWaiter.isPresent()) {
                        WaitingListEntity w = firstWaiter.get();

                        // 1. 다른 사람이 못 쓰게 주차칸을 '예약중(reserved)'으로 묶어버림
                        zone.setStatus("reserved");
                        parkingZoneRepository.save(zone);

                        // 2. 1등에게만 알림 발송 (대기권 사용 완료 처리)
                        String msg = "대기하시던 [" + update.getSlot() + "] 구역에 빈자리가 생겼습니다! 3분 안에 주차해 주세요.";
                        w.setNotified(true);

                        notificationRepository.save(AppNotificationEntity.builder()
                                .resident(w.getResident()).type("system").title("🔔 주차 예약 알림").message(msg).read(false).build());

                        boolean isPushOn = settingRepository.findByDeviceId("device_" + w.getResident().getNo())
                                .map(AppSettingEntity::getAlertPush).orElse(true);
                        if (isPushOn) {
                            deviceInfoRepository.findByResident_No(w.getResident().getNo())
                                    .forEach(d -> fcmService.sendPush(d.getFcmToken(), "🔔 3분 주차 예약", msg));
                        }

                        // 3. 3분 뒤에 확인하는 타이머 작동! (테스트할 땐 1분으로 줄여보세요)
                        scheduler.schedule(() -> {
                            // 3분 뒤 다시 DB를 확인해서 여전히 'reserved' 상태인지 확인 (차를 안 댔다면)
                            parkingZoneRepository.findByAreaNumber(update.getSlot()).ifPresent(checkZone -> {
                                if ("reserved".equals(checkZone.getStatus())) {
                                    System.out.println("🚨 3분 경과! 1등 예약이 취소되고 다음 사람에게 넘어갑니다.");

                                    // 철벽 방어를 뚫기 위해 DB를 수동으로 빈자리로 돌려놓음
                                    checkZone.setStatus("empty");
                                    parkingZoneRepository.saveAndFlush(checkZone);

                                    // 다음 대기자(2등)에게 기회를 주기 위해, 서버가 스스로 빈자리 신호를 다시 쏩니다! (재귀 호출)
                                    AppParkingUpdateRequestDto retryDto = new AppParkingUpdateRequestDto();
                                    AppParkingUpdateItemDto retryItem = new AppParkingUpdateItemDto();
                                    retryItem.setSlot(update.getSlot());
                                    retryItem.setStatus("empty");
                                    retryDto.setUpdates(Collections.singletonList(retryItem));

                                     // ✅ 변경 후: self를 붙여서 부르면 트랜잭션이 완벽하게 유지됩니다!
                                     self.updateParking(retryDto);
                                }
                            });
                        }, 3, TimeUnit.MINUTES); // 👈 (여기를 1로 바꾸면 1분 타이머가 됩니다)
                    }
                }
                // =========================================================
                // 💡 [주차 완료 시] 차를 대면 'occupied'로 덮어씌워지며 예약이 자동으로 종료됨
                // =========================================================
                else if ((update.getStatus().equals("occupied") || update.getStatus().equals("사용중")) && zone.getCurrentCarNumber() != null) {
                    residentVehicleRepository.findByNumber(zone.getCurrentCarNumber()).ifPresent(car -> {
                        String msg = "[" + update.getSlot() + "] 구역에 차량(" + car.getNumber() + ") 주차가 완료되었습니다.";
                        notificationRepository.save(AppNotificationEntity.builder()
                                .resident(car.getResident()).type("system").title("🅿️ 주차 완료 알림").message(msg).read(false).build());

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
    // 👇 파일 맨 밑의 마지막 괄호(}) 바로 위에 이 함수를 통째로 추가하세요!
    @PreDestroy
    public void destroyTimer() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("주차 3분 예약 타이머가 안전하게 종료되었습니다.");
        }
    }
}

