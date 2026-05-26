package python.service;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import python.dto.PythonParkingEntryRequestDto;
import python.dto.PythonParkingExitRequestDto;
import python.dto.PythonParkingPlateUpdateRequestDto;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonParkingEventService {

    private static final String STATUS_EMPTY = "empty";
    private static final String STATUS_OCCUPIED = "occupied";
    private static final String HISTORY_PARKED = "PARKED";
    private static final String HISTORY_EXITED = "EXITED";
    private static final String UNKNOWN_PLATE = "UNKNOWN";
    private static final DateTimeFormatter PYTHON_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParkingZoneRepository parkingZoneRepository;
    private final ParkingHistoryRepository parkingHistoryRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;

    private final AppNotificationRepository notificationRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final WaitingListRepository waitingListRepository;
    private final FcmService fcmService;

    public List<Map<String, String>> findCarNumbers() {
        Set<String> carNumbers = new LinkedHashSet<>();
        residentVehicleRepository.findAll().forEach(vehicle -> addCarNumber(carNumbers, vehicle.getNumber()));
        registeredCarRepository.findAll().forEach(vehicle -> addCarNumber(carNumbers, vehicle.getNumber()));

        List<Map<String, String>> response = new ArrayList<>();
        carNumbers.forEach(number -> response.add(Map.of("c_number", number)));
        return response;
    }

    public Map<String, Object> findZoneStatus(String zoneName) {
        ParkingZoneEntity zone = findZone(zoneName);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("zone", zone.getAreaNumber());
        response.put("status_type", zone.getStatus());
        response.put("current_car_number", zone.getCurrentCarNumber());
        return response;
    }

    @Transactional
    public Map<String, Object> saveEntry(PythonParkingEntryRequestDto requestDto) {
        validateZone(requestDto != null ? requestDto.getZone() : null);
        ParkingZoneEntity zone = findZone(requestDto.getZone());

        if (STATUS_OCCUPIED.equals(zone.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 주차 중인 구역입니다.");
        }

        String plate = normalizePlate(requestDto.getPlate());
        ResidentVehicleEntity residentVehicle = findResidentVehicle(plate);
        RegisteredCarEntity visitorVehicle = findVisitorVehicle(plate);

        zone.setStatus(STATUS_OCCUPIED);
        zone.setCurrentCarNumber(plate);
        zone.setStatusChangeReason("Python 객체인식 입차 이벤트");

        if (visitorVehicle != null && visitorVehicle.getParkedAt() == null) {
            visitorVehicle.setParkedAt(parseDateTime(requestDto.getEntryTime()));
        }

        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .parkingZone(zone)
                .residentVehicle(residentVehicle)
                .visitorVehicle(visitorVehicle)
                .zoneSnapshot(zone.getAreaNumber())
                .plate(plate)
                .entryTime(parseDateTime(requestDto.getEntryTime()))
                .status(HISTORY_PARKED)
                .build();

        parkingHistoryRepository.save(history);
        // 💡 [추가] 주차 완료 FCM 푸시 알림 발송
    ResidentEntity owner = null;
    if (residentVehicle != null) owner = residentVehicle.getResident();
    else if (visitorVehicle != null) owner = visitorVehicle.getResident();

    if (owner != null) {
        String msg = "[" + zone.getAreaNumber() + "] 구역에 차량(" + plate + ") 주차가 완료되었습니다.";
        ResidentEntity finalOwner = owner;
        
        notificationRepository.save(AppNotificationEntity.builder()
                .resident(finalOwner).type("system").title("🅿️ 주차 완료 알림").message(msg).read(false).build());

        deviceInfoRepository.findByResident_No(finalOwner.getNo())
                .forEach(d -> fcmService.sendPush(d.getFcmToken(), "🅿️ 주차 완료 알림", msg));
    }
        return result("entry", zone, history);
    }

    @Transactional
    public Map<String, Object> saveExit(PythonParkingExitRequestDto requestDto) {
        validateZone(requestDto != null ? requestDto.getZone() : null);
        ParkingZoneEntity zone = findZone(requestDto.getZone());
        ParkingHistoryEntity history = findActiveHistory(zone);

        LocalDateTime exitTime = parseDateTime(requestDto.getExitTime());
        history.setExitTime(exitTime);
        history.setStatus(HISTORY_EXITED);

        zone.setStatus(STATUS_EMPTY);
        zone.setCurrentCarNumber(null);
        zone.setStatusChangeReason("Python 객체인식 출차 이벤트");
// 💡 [추가] 빈자리 발생 FCM 대기자 푸시 알림 발송
        waitingListRepository.findAll().stream()
            .filter(w -> !w.getNotified() && (w.getTargetSlotId().equals("ALL") || w.getTargetSlotId().equals(zone.getAreaNumber())))
            .forEach(w -> {
                w.setNotified(true); // 알림 발송 완료 처리
                String msg = "대기하시던 [" + zone.getAreaNumber() + "] 구역에 빈자리가 생겼습니다! 먼저 주차하세요.";
                
                notificationRepository.save(AppNotificationEntity.builder()
                        .resident(w.getResident()).type("system").title("🔔 빈자리 알림").message(msg).read(false).build());

                deviceInfoRepository.findByResident_No(w.getResident().getNo())
                        .forEach(d -> fcmService.sendPush(d.getFcmToken(), "🔔 빈자리 알림", msg));
            });
        return result("exit", zone, history);
    }

    @Transactional
    public Map<String, Object> updatePlate(PythonParkingPlateUpdateRequestDto requestDto) {
        validateZone(requestDto != null ? requestDto.getZone() : null);
        ParkingZoneEntity zone = findZone(requestDto.getZone());
        ParkingHistoryEntity history = findActiveHistory(zone);

        String plate = normalizePlate(requestDto.getPlate());
        history.setPlate(plate);
        history.setResidentVehicle(findResidentVehicle(plate));
        history.setVisitorVehicle(findVisitorVehicle(plate));

        zone.setCurrentCarNumber(plate);
        zone.setStatusChangeReason("Python 객체인식 번호판 업데이트");

        return result("update", zone, history);
    }

    private ParkingZoneEntity findZone(String zoneName) {
        validateZone(zoneName);
        return parkingZoneRepository.findByAreaNumber(zoneName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주차구역입니다."));
    }

    private ParkingHistoryEntity findActiveHistory(ParkingZoneEntity zone) {
        return parkingHistoryRepository
                .findFirstByParkingZone_NoAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(zone.getNo(), HISTORY_PARKED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "진행 중인 주차 기록이 없습니다."));
    }

    private ResidentVehicleEntity findResidentVehicle(String plate) {
        if (UNKNOWN_PLATE.equals(plate)) {
            return null;
        }
        return residentVehicleRepository.findByNumber(plate).orElse(null);
    }

    private RegisteredCarEntity findVisitorVehicle(String plate) {
        if (UNKNOWN_PLATE.equals(plate)) {
            return null;
        }
        return registeredCarRepository.findFirstByNumberAndParkedAtIsNull(plate).orElse(null);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(value, PYTHON_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value);
        }
    }

    private String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return UNKNOWN_PLATE;
        }
        return plate.trim();
    }

    private void validateZone(String zoneName) {
        if (zoneName == null || zoneName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주차구역 번호는 필수입니다.");
        }
    }

    private void addCarNumber(Set<String> carNumbers, String number) {
        if (number != null && !number.isBlank()) {
            carNumbers.add(number.trim());
        }
    }

    private Map<String, Object> result(String event, ParkingZoneEntity zone, ParkingHistoryEntity history) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("event", event);
        response.put("zone", zone.getAreaNumber());
        response.put("status", zone.getStatus());
        response.put("plate", history.getPlate());
        response.put("history_id", history.getId());
        return response;
    }
}
