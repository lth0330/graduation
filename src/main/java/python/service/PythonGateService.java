package python.service;

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
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ResidentVehicleRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonGateService {

    private static final String HISTORY_PARKED = "PARKED";
    private static final String UNKNOWN_PLATE = "UNKNOWN";
    private static final DateTimeFormatter PYTHON_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;
    private final ParkingHistoryRepository parkingHistoryRepository;
    private final GateEntryLogRepository gateEntryLogRepository;

    public Map<String, Object> checkPlate(String plate) {
        String normalizedPlate = normalizePlate(plate);
        boolean isRegistered = normalizedPlate != null
                && (residentVehicleRepository.existsByNumber(normalizedPlate)
                || registeredCarRepository.existsByNumber(normalizedPlate)
                || existsByCompactPlate(normalizedPlate));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("is_resident", isRegistered);
        return response;
    }

    @Transactional
    public Map<String, Object> saveGateLog(Map<String, Object> request) {
        String plate = normalizePlate(firstText(request, "gate_plate", "c_number", "plate"));
        if (plate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plate or c_number is required.");
        }

        boolean isResident = firstBoolean(request, "gate_is_resident", "is_resident");
        boolean gateOpen = firstBoolean(request, "gate_open") || isResident;
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
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("history_id", history.getId());
        response.put("plate", history.getPlate());
        response.put("zone", history.getZoneSnapshot());
        return response;
    }

    public Map<String, Object> saveDoubleParkingAlert(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("saved", false);
        response.put("plate", firstText(request, "plate", "c_number"));
        response.put("candidates", firstText(request, "candidates"));
        response.put("message", "double_park_alert 테이블 없이 요청 수신만 처리했습니다.");
        return response;
    }

    private String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return null;
        }
        return plate.trim();
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

    private Integer firstInteger(Map<String, Object> request, String key) {
        if (request == null || request.get(key) == null) {
            return null;
        }
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
