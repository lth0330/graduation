package python.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
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

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import python.dto.PythonParkingEntryRequestDto;
import python.dto.PythonParkingExitRequestDto;
import python.dto.PythonParkingPlateUpdateRequestDto;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.notification.service.ManagerNotificationService;

// 👇 [핵심 1] 앱의 주차 로직(3분 예약, 알림 설정 확인 등)을 그대로 가져다 쓰기 위한 도구들
import app.service.AppResidentFeatureService;
import app.dto.AppParkingUpdateRequestDto;
import app.dto.AppParkingUpdateItemDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonParkingEventService {

    private static final String STATUS_EMPTY = "empty";
    private static final String STATUS_OCCUPIED = "occupied";
    private static final String ZONE_TYPE_NORMAL = "normal";
    private static final String ZONE_TYPE_DOUBLE_LANE = "double_lane";
    private static final String PARK_TYPE_NORMAL = "normal";
    private static final String PARK_TYPE_MULTI_ZONE = "multi_zone";
    private static final String PARK_TYPE_DOUBLE_LANE = "double_lane";
    private static final String HISTORY_PARKED = "PARKED";
    private static final String HISTORY_EXITED = "EXITED";
    private static final String UNKNOWN_PLATE = "UNKNOWN";
    private static final DateTimeFormatter PYTHON_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParkingZoneRepository parkingZoneRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingHistoryRepository parkingHistoryRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;
    private final ManagerNotificationService managerNotificationService;

    // 💡 [핵심 2] 복잡한 알림 도구들을 다 지우고, 대신 '앱 서비스'를 통째로 불러옵니다!
    private final AppResidentFeatureService appResidentFeatureService;

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

    // FastAPI 차단기 제어용 전체 주차장 점유율을 계산한다.
    public Map<String, Object> findOccupancy() {
        List<ParkingLotEntity> parkingLots = parkingLotRepository.findAll();
        int total = parkingLots.stream()
                .map(ParkingLotEntity::getTotalSpaces)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .sum();
        int used = parkingLots.stream()
                .map(ParkingLotEntity::getUsedSpaces)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .sum();
        int available = Math.max(total - used, 0);
        double rate = total > 0 ? (double) used / total : 0.0;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total", total);
        response.put("used", used);
        response.put("available", available);
        response.put("rate", rate);
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
        String parkType = normalizeParkType(requestDto.getParkType(), zone);
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
                .parkType(parkType)
                .linkedZone(normalizeLinkedZone(requestDto.getLinkedZone()))
                .build();

        ParkingHistoryEntity savedHistory = parkingHistoryRepository.save(history);

        // =========================================================
        // 💡 [핵심 3] 복잡한 푸시 알림 코드를 싹 지우고, 앱 서비스에게 입차 사실만 넘깁니다!
        // 그러면 앱 서비스가 알아서 방해금지 설정(ON/OFF) 확인하고 알림을 예쁘게 쏴줍니다.
        // =========================================================
        AppParkingUpdateRequestDto req = new AppParkingUpdateRequestDto();
        AppParkingUpdateItemDto item = new AppParkingUpdateItemDto();
        item.setSlot(zone.getAreaNumber());
        item.setStatus(STATUS_OCCUPIED);
        req.setUpdates(Collections.singletonList(item));
        appResidentFeatureService.updateParking(req);

        createParkingNotificationIfNeeded(zone, savedHistory);
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

        // =========================================================
        // 💡 [핵심 4] 옛날 방식의 전체 발송 코드를 지우고, 앱 서비스에게 넘깁니다!
        // 이제 카메라가 차가 빠진 걸 인식하면, 서버가 1등 대기자를 찾아 3분 타이머를 완벽하게 돌립니다.
        // =========================================================
        AppParkingUpdateRequestDto req = new AppParkingUpdateRequestDto();
        AppParkingUpdateItemDto item = new AppParkingUpdateItemDto();
        item.setSlot(zone.getAreaNumber());
        item.setStatus(STATUS_EMPTY);
        req.setUpdates(Collections.singletonList(item));
        appResidentFeatureService.updateParking(req);

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

    private String normalizeParkType(String requestedParkType, ParkingZoneEntity zone) {
        if (requestedParkType != null && !requestedParkType.isBlank()) {
            return requestedParkType.trim();
        }
        if (ZONE_TYPE_DOUBLE_LANE.equals(zone.getZoneType())) {
            return PARK_TYPE_DOUBLE_LANE;
        }
        return PARK_TYPE_NORMAL;
    }

    private String normalizeLinkedZone(String linkedZone) {
        if (linkedZone == null || linkedZone.isBlank()) {
            return null;
        }
        return linkedZone.trim();
    }

    private void createParkingNotificationIfNeeded(ParkingZoneEntity zone, ParkingHistoryEntity history) {
        boolean multiZoneParking = PARK_TYPE_MULTI_ZONE.equals(history.getParkType());
        boolean doubleLaneParking = ZONE_TYPE_DOUBLE_LANE.equals(zone.getZoneType())
                || PARK_TYPE_DOUBLE_LANE.equals(history.getParkType());

        if (!multiZoneParking && !doubleLaneParking) {
            return;
        }

        boolean hasEmptyNormalZone = zone.getParkingLot() != null
                && parkingZoneRepository.existsByParkingLot_NoAndZoneTypeAndStatus(
                zone.getParkingLot().getNo(),
                ZONE_TYPE_NORMAL,
                STATUS_EMPTY
        );

        if (doubleLaneParking && !hasEmptyNormalZone) {
            return;
        }

        String title = multiZoneParking ? "두 칸 주차 감지" : "이중주차 구역 주차 감지";
        String message = buildParkingNotificationMessage(zone, history, multiZoneParking);

        managerNotificationService.createApartmentNotification(
                zone.getParkingLot() != null ? zone.getParkingLot().getApartment() : null,
                "abnormal_parking",
                title,
                message,
                "parking_history",
                history.getId()
        );
    }

    private String buildParkingNotificationMessage(
            ParkingZoneEntity zone,
            ParkingHistoryEntity history,
            boolean multiZoneParking
    ) {
        if (multiZoneParking) {
            return zone.getAreaNumber() + " 구역에서 두 칸 주차가 감지되었습니다.";
        }
        return zone.getAreaNumber() + " 구역에 정상 주차칸이 남아있는 상태로 이중주차가 감지되었습니다.";
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
        response.put("park_type", history.getParkType());
        response.put("linked_zone", history.getLinkedZone());
        response.put("history_id", history.getId());
        return response;
    }
}