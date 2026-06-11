\package python.service;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import python.entity.GateEntryLogEntity;
import python.repository.GateEntryLogRepository;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.notification.entity.ManagerNotificationEntity;
import web.notification.service.ManagerNotificationService;
import app.service.AppResidentFeatureService; // 👈 이 줄을 import 모음에 추가하세요!


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonGateService {

    private static final String HISTORY_PARKED = "PARKED";
    private static final String UNKNOWN_PLATE = "UNKNOWN";
    private static final String STATUS_OCCUPIED = "occupied";
    private static final String ZONE_TYPE_NORMAL = "normal";
    private static final double GATE_OCCUPANCY_BLOCK_RATE = 0.8;
    private static final DateTimeFormatter PYTHON_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;
    private final ParkingHistoryRepository parkingHistoryRepository;
    private final GateEntryLogRepository gateEntryLogRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingZoneRepository parkingZoneRepository;
    private final ApartmentRepository apartmentRepository;
    private final ManagerNotificationService managerNotificationService;
    private final AppResidentFeatureService appResidentFeatureService; // 👈 추가!
    // 주민 차량과 방문 차량 테이블을 모두 확인하고 관리자 차단 정책까지 반영해 차단기 개방 여부를 만든다.
    @Transactional
    public Map<String, Object> checkPlate(String plate, Integer apartmentNo) {
        String normalizedPlate = normalizePlate(plate);
        ResidentVehicleEntity residentVehicle = normalizedPlate != null ? findResidentVehicle(normalizedPlate) : null;
        RegisteredCarEntity visitorVehicle = normalizedPlate != null ? findVisitorVehicle(normalizedPlate) : null;
        boolean isResidentVehicle = residentVehicle != null;
        boolean isVisitorVehicle = visitorVehicle != null;
        boolean isRegistered = isResidentVehicle || isVisitorVehicle;

        ApartmentEntity apartment = findGateApartment(apartmentNo, residentVehicle, visitorVehicle);
        boolean occupancyBlockEnabled = isOccupancyBlockEnabled(apartment);
        boolean forceOpenEnabled = isForceOpenEnabled(apartment);
        Occupancy occupancy = calculateOccupancy(apartment);
        boolean full = occupancy.available() <= 0 && occupancy.total() > 0;
        boolean overThreshold = occupancy.rate() >= GATE_OCCUPANCY_BLOCK_RATE;
        boolean blockedByOccupancy = isVisitorVehicle && occupancyBlockEnabled && (full || overThreshold);
        boolean gateOpen = forceOpenEnabled || isResidentVehicle || (isVisitorVehicle && !blockedByOccupancy);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apartment_no", apartment != null ? apartment.getNo() : null);
        response.put("plate", normalizedPlate);
        // 기존 Python 코드 호환용: 현재는 주민/방문 등록 차량이면 true로 사용한다.
        response.put("is_resident", isRegistered);
        response.put("is_registered", isRegistered);
        response.put("is_resident_vehicle", isResidentVehicle);
        response.put("is_visitor", isVisitorVehicle);
        response.put("gate_open", gateOpen);
        response.put("occupancy_block_enabled", occupancyBlockEnabled);
        response.put("force_open_enabled", forceOpenEnabled);
        response.put("total", occupancy.total());
        response.put("used", occupancy.used());
        response.put("available", occupancy.available());
        response.put("rate", occupancy.rate());
        response.put("reason", buildGateReason(
                isRegistered,
                isResidentVehicle,
                isVisitorVehicle,
                gateOpen,
                occupancyBlockEnabled,
                forceOpenEnabled,
                full,
                overThreshold
        ));
        sendGateEntryNotificationIfNeeded(residentVehicle, gateOpen);
        return response;
    }

    public Map<String, Object> findGateControl(Integer apartmentNo) {
        ApartmentEntity apartment = findGateApartment(apartmentNo, null, null);
        boolean forceOpenEnabled = isForceOpenEnabled(apartment);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apartment_no", apartment != null ? apartment.getNo() : null);
        response.put("gate_force_open_enabled", forceOpenEnabled);
        response.put("gate_open", forceOpenEnabled);
        response.put("mode", forceOpenEnabled ? "FORCE_OPEN" : "NORMAL");
        response.put("reason", forceOpenEnabled
                ? "관리자 설정에 따라 차단기를 상시 개방합니다."
                : "차단기가 일반 번호판 확인 모드로 동작합니다.");
        return response;
    }

    @Transactional
    // 차단기 통과 기록을 gate_entry_log 테이블에 저장한다.
    public Map<String, Object> saveGateLog(Map<String, Object> request) {
        String plate = normalizePlate(firstText(request, "gate_plate", "c_number", "plate"));
        if (plate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plate or c_number is required.");
        }

        boolean isResident = firstBoolean(request, "gate_is_resident", "is_resident", "is_registered");
        boolean gateOpen = hasKey(request, "gate_open") ? firstBoolean(request, "gate_open") : isResident;
        LocalDateTime gateTime = parseDateTime(firstText(request, "gate_time", "entry_time", "time"));

        GateEntryLogEntity savedLog = gateEntryLogRepository.save(GateEntryLogEntity.builder()
                .plate(plate)
                .resident(isResident)
                .gateOpen(gateOpen)
                .gateTime(gateTime)
                .build());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("log_no", savedLog.getLogNo());
        response.put("plate", savedLog.getPlate());
        response.put("is_resident", isResident);
        response.put("gate_open", gateOpen);
        response.put("gate_time", savedLog.getGateTime());
        response.put("saved", true);
        return response;
    }

    // 번호판이 UNKNOWN인 진행 중 주차 기록을 찾아 차단기 인식 결과와 매칭할 수 있게 한다.
    public List<Map<String, Object>> findUnmatchedHistories() {
        List<ParkingHistoryEntity> histories = parkingHistoryRepository
                .findByPlateAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(UNKNOWN_PLATE, HISTORY_PARKED);

        List<Map<String, Object>> response = new ArrayList<>();
        for (ParkingHistoryEntity history : histories) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("history_id", history.getId());
            item.put("history_zone", history.getZoneSnapshot());
            item.put("history_plate", history.getPlate());
            response.add(item);
        }
        return response;
    }

    @Transactional
    // 차단기에서 확인한 번호판을 기존 주차 이력과 주차칸 상태에 반영한다.
    public Map<String, Object> assignPlate(Map<String, Object> request) {
        Integer historyId = firstInteger(request, "history_id");
        String plate = normalizePlate(firstText(request, "plate", "c_number", "gate_plate"));

        if (historyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "history_id는 필수입니다.");
        }
        if (plate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plate는 필수입니다.");
        }

        ParkingHistoryEntity history = parkingHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주차 기록을 찾을 수 없습니다."));

        history.setPlate(plate);
        history.setResidentVehicle(findResidentVehicle(plate));
        history.setVisitorVehicle(registeredCarRepository.findFirstByNumberAndParkedAtIsNull(plate).orElse(null));

        ParkingZoneEntity zone = history.getParkingZone();
        if (zone != null) {
            zone.setCurrentCarNumber(plate);
            zone.setStatusChangeReason("차단기 인식 번호판 자동 매칭");

            // =========================================================
            // 👇 [새로 추가된 코드] 매칭된 구역이 '통로'인 경우 100% 확정 알림!
            // =========================================================
            if (zone.getAreaNumber().contains("통로") || zone.getAreaNumber().matches(".*a-b1-00[789].*")) {
                if (history.getResidentVehicle() != null && history.getResidentVehicle().getResident() != null) {
                    appResidentFeatureService.sendPushToResident(
                            history.getResidentVehicle().getResident().getNo(),
                            "🚨 이중주차 알림",
                            "[" + zone.getAreaNumber() + "] 구역에 이중주차가 확인되었습니다. 이동 주차 바랍니다."
                    );
                }
            }
            // =========================================================
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("history_id", history.getId());
        response.put("plate", history.getPlate());
        response.put("zone", history.getZoneSnapshot());
        return response;
    }

    @Transactional
    // Python에서 보낸 OCR 실패/이상 알림을 관리자 웹 알림으로 저장한다.
    public Map<String, Object> saveDoubleParkingAlert(Map<String, Object> request) {
        String alertType = firstText(request, "type", "alert_type");
        String normalizedType = limitText(alertType != null ? alertType.trim() : "gate_alert", 30);
        String plate = normalizePlate(firstText(request, "plate", "c_number", "gate_plate"));
        String candidates = firstText(request, "candidates", "message");
        String imagePath = firstText(request, "image_path", "imagePath", "snapshot_path");
        Integer apartmentNo = firstInteger(request, "apartment_no", "apartmentNo", "a_no");
        Integer historyId = firstInteger(request, "history_id", "historyId");
        String zoneName = firstText(request, "zone", "history_zone", "parking_zone");
        String eventTime = firstText(request, "time", "alert_time", "entry_time", "created_at");

        ApartmentEntity apartment = findAlertApartment(apartmentNo, historyId, zoneName);
        if (apartment == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "알림을 저장할 아파트 정보를 찾을 수 없습니다.");
        }

        boolean ocrError = "ocr_error".equalsIgnoreCase(normalizedType);
        boolean assignFail = "assign_fail".equalsIgnoreCase(normalizedType);
        String title = buildAlertTitle(ocrError, assignFail);
        String message = buildGateAlertMessage(ocrError, zoneName, eventTime, plate, candidates, imagePath);
        String referenceType = ocrError && historyId != null ? "parking_history" : "gate_alert";
        Integer referenceId = ocrError && historyId != null
                ? historyId
                : stableAlertReferenceId(normalizedType, zoneName, candidates, plate);

        ManagerNotificationEntity notification = managerNotificationService.createApartmentNotification(
                apartment,
                normalizedType,
                title,
                message,
                referenceType,
                referenceId
        );
// =========================================================
        // 👇 [새로 추가된 코드] 구역이 '통로'이고 파이썬이 보내준 후보자(candidates)가 있다면 의심 알림!
        // =========================================================
        if (zoneName != null && (zoneName.contains("통로") || zoneName.matches(".*a-b1-00[789].*"))) {
            if (candidates != null && !candidates.isEmpty() && !ocrError) {
                // 파이썬이 "12가1234,34나5678" 처럼 쉼표로 보낸 용의자 번호판들을 분리합니다.
                String[] candidatePlates = candidates.split(",");
                for (String cp : candidatePlates) {
                    ResidentVehicleEntity rv = findResidentVehicle(cp.trim());
                    if (rv != null && rv.getResident() != null) {
                        appResidentFeatureService.sendPushToResident(
                                rv.getResident().getNo(),
                                "🚨 이중주차 의심 알림",
                                "[" + zoneName + "] 구역 이중주차 차량으로 의심됩니다. 본인 차량일 경우 이동 주차 바랍니다."
                        );
                    }
                }
            }
        }
        // =========================================================
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("saved", notification != null);
        response.put("notification_no", notification != null ? notification.getNo() : null);
        response.put("apartment_no", apartment.getNo());
        response.put("type", normalizedType);
        response.put("zone", zoneName);
        response.put("time", eventTime);
        response.put("plate", plate);
        response.put("candidates", candidates);
        response.put("image_path", imagePath);
        response.put("message", "관리자 알림으로 저장했습니다.");
        return response;
    }

    private ApartmentEntity findAlertApartment(Integer apartmentNo, Integer historyId, String zoneName) {
        if (apartmentNo != null) {
            return apartmentRepository.findById(apartmentNo).orElse(null);
        }
        if (historyId != null) {
            return parkingHistoryRepository.findById(historyId)
                    .map(ParkingHistoryEntity::getParkingZone)
                    .map(ParkingZoneEntity::getParkingLot)
                    .map(ParkingLotEntity::getApartment)
                    .orElse(null);
        }
        if (zoneName != null) {
            return parkingZoneRepository.findByAreaNumber(zoneName)
                    .map(ParkingZoneEntity::getParkingLot)
                    .map(ParkingLotEntity::getApartment)
                    .orElse(null);
        }
        return findGateApartment(null, null, null);
    }

    private String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return null;
        }
        String compactPlate = compact(plate);
        return compactPlate.isBlank() ? null : compactPlate;
    }

    private boolean existsByCompactPlate(String plate) {
        String compactPlate = compact(plate);
        return residentVehicleRepository.findAll()
                .stream()
                .anyMatch(vehicle -> compact(vehicle.getNumber()).equals(compactPlate))
                || registeredCarRepository.findAll()
                .stream()
                .anyMatch(vehicle -> compact(vehicle.getNumber()).equals(compactPlate));
    }

    private String compact(String plate) {
        if (plate == null) {
            return "";
        }
        return plate.replaceAll("\\s+", "");
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

    private ResidentVehicleEntity findResidentVehicle(String plate) {
        return residentVehicleRepository.findByNumber(plate)
                .orElseGet(() -> residentVehicleRepository.findAll()
                        .stream()
                        .filter(vehicle -> compact(vehicle.getNumber()).equals(compact(plate)))
                        .findFirst()
                        .orElse(null));
    }

    private RegisteredCarEntity findVisitorVehicle(String plate) {
        if (plate == null) {
            return null;
        }
        return registeredCarRepository.findFirstByNumber(plate)
                .orElseGet(() -> registeredCarRepository.findAll()
                        .stream()
                        .filter(vehicle -> compact(vehicle.getNumber()).equals(compact(plate)))
                        .findFirst()
                        .orElse(null));
    }

    private ApartmentEntity findGateApartment(
            Integer apartmentNo,
            ResidentVehicleEntity residentVehicle,
            RegisteredCarEntity visitorVehicle
    ) {
        if (apartmentNo != null) {
            return apartmentRepository.findById(apartmentNo).orElse(null);
        }
        if (residentVehicle != null && residentVehicle.getResident() != null) {
            return residentVehicle.getResident().getApartment();
        }
        if (visitorVehicle != null && visitorVehicle.getResident() != null) {
            return visitorVehicle.getResident().getApartment();
        }
        List<ApartmentEntity> apartments = apartmentRepository.findAll();
        return apartments.size() == 1 ? apartments.get(0) : null;
    }

    private boolean isOccupancyBlockEnabled(ApartmentEntity apartment) {
        return apartment == null
                || apartment.getGateOccupancyBlockEnabled() == null
                || apartment.getGateOccupancyBlockEnabled();
    }

    private boolean isForceOpenEnabled(ApartmentEntity apartment) {
        return apartment != null
                && apartment.getGateForceOpenEnabled() != null
                && apartment.getGateForceOpenEnabled();
    }

    private void sendGateEntryNotificationIfNeeded(ResidentVehicleEntity residentVehicle, boolean gateOpen) {
        if (!gateOpen || residentVehicle == null || residentVehicle.getResident() == null) {
            return;
        }
        Integer residentNo = residentVehicle.getResident().getNo();
        if (residentNo == null) {
            return;
        }
        appResidentFeatureService.sendPushToResident(
                residentNo,
                "🚗 입차 알림",
                residentVehicle.getNumber() + " 차량이 입구를 통과했습니다."
        );
    }

    private Occupancy calculateOccupancy(ApartmentEntity apartment) {
        List<ParkingLotEntity> parkingLots = apartment != null && apartment.getNo() != null
                ? parkingLotRepository.findByApartment_No(apartment.getNo())
                : parkingLotRepository.findAll();

        List<ParkingZoneEntity> normalZones = parkingLots.stream()
                .filter(parkingLot -> parkingLot.getNo() != null)
                .flatMap(parkingLot -> parkingZoneRepository.findByParkingLot_No(parkingLot.getNo()).stream())
                .filter(this::isNormalZone)
                .toList();

        int total = normalZones.size();
        int used = (int) normalZones.stream()
                .filter(zone -> STATUS_OCCUPIED.equals(zone.getStatus()))
                .count();
        int available = Math.max(total - used, 0);
        double rate = total > 0 ? (double) used / total : 0.0;
        return new Occupancy(total, used, available, rate);
    }

    private boolean isNormalZone(ParkingZoneEntity zone) {
        return zone != null
                && (zone.getZoneType() == null
                || zone.getZoneType().isBlank()
                || ZONE_TYPE_NORMAL.equals(zone.getZoneType()));
    }

    private String buildGateReason(
            boolean isRegistered,
            boolean isResidentVehicle,
            boolean isVisitorVehicle,
            boolean gateOpen,
            boolean occupancyBlockEnabled,
            boolean forceOpenEnabled,
            boolean full,
            boolean overThreshold
    ) {
        if (forceOpenEnabled) {
            return "관리자 설정에 따라 차단기를 상시 개방합니다.";
        }
        if (!isRegistered) {
            return "등록되지 않은 차량입니다.";
        }
        if (isResidentVehicle) {
            return "입주민 등록 차량은 주차장 점유율과 관계없이 개방합니다.";
        }
        if (gateOpen && !occupancyBlockEnabled) {
            return "관리자 설정에 따라 방문차량도 번호판 등록 여부만 확인했습니다.";
        }
        if (gateOpen && isVisitorVehicle) {
            return "방문 등록 차량이며 주차장 혼잡도 조건을 통과했습니다.";
        }
        if (full) {
            return "주차장이 만차라 방문차량은 입차할 수 없습니다.";
        }
        if (overThreshold) {
            return "주차장 점유율이 80% 이상이라 방문차량은 입차할 수 없습니다.";
        }
        return "차단기 개방 조건을 만족하지 않습니다.";
    }

    private String buildGateAlertMessage(
            boolean ocrError,
            String zoneName,
            String eventTime,
            String plate,
            String candidates,
            String imagePath
    ) {
        StringBuilder message = new StringBuilder();
        if (ocrError) {
            if (zoneName != null) {
                message.append(zoneName).append(" 구역 번호판 인식 실패. 관리자 확인 필요.");
            } else {
                message.append("주차 구역 번호판 인식 실패. 관리자 확인 필요.");
            }
        } else if (candidates != null && !candidates.isBlank()) {
            message.append("후보 차량 [").append(candidates).append("] 중 자동 연결할 주차 기록을 확정하지 못했습니다.");
        } else {
            message.append("차단기 또는 주차 인식 결과 확인이 필요합니다.");
        }
        if (!ocrError && zoneName != null) {
            message.append(" 구역: ").append(zoneName).append(".");
        }
        if (eventTime != null) {
            message.append(" 시간: ").append(eventTime).append(".");
        }
        if (plate != null) {
            message.append(" 차량번호: ").append(plate).append(".");
        }
        if (candidates != null) {
            message.append(" 내용: ").append(candidates).append(".");
        }
        if (imagePath != null) {
            message.append(" 이미지: ").append(imagePath);
        }
        return limitText(message.toString(), 255);
    }

    private String buildAlertTitle(boolean ocrError, boolean assignFail) {
        if (ocrError) {
            return "번호판 인식 실패";
        }
        if (assignFail) {
            return "번호판 자동 연결 실패";
        }
        return "주차 인식 확인 필요";
    }

    private Integer stableAlertReferenceId(String type, String zoneName, String candidates, String plate) {
        String key = String.join(
                "|",
                type != null ? type : "",
                zoneName != null ? zoneName : "",
                candidates != null ? candidates : "",
                plate != null ? plate : ""
        );
        return Math.abs(key.hashCode());
    }

    private String limitText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private boolean hasKey(Map<String, Object> request, String key) {
        return request != null && request.containsKey(key);
    }

    private record Occupancy(int total, int used, int available, double rate) {
    }

    private String firstText(Map<String, Object> request, String... keys) {
        if (request == null) {
            return null;
        }
        for (String key : keys) {
            Object value = request.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    private boolean firstBoolean(Map<String, Object> request, String... keys) {
        if (request == null) {
            return false;
        }
        for (String key : keys) {
            Object value = request.get(key);
            if (value instanceof Boolean booleanValue) {
                return booleanValue;
            }
            if (value != null && !value.toString().isBlank()) {
                return Boolean.parseBoolean(value.toString());
            }
        }
        return false;
    }

    private Integer firstInteger(Map<String, Object> request, String... keys) {
        if (request == null) {
            return null;
        }
        for (String key : keys) {
            Object value = request.get(key);
            if (value == null || value.toString().isBlank()) {
                continue;
            }
            if (value instanceof Number number) {
                return number.intValue();
            }
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
