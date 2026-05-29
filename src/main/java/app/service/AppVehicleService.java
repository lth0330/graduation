package app.service;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import app.dto.AppCarSaveRequestDto;
import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 앱 차량 서비스: 입주민 차량(car)과 방문 차량(registered_cars)의 CRUD를 처리한다.
public class AppVehicleService {

    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;

    public Map<String, Object> findCars(Integer residentNo) {
        Map<String, Object> response = success();
        // 앱 화면은 입주민 차량과 방문 차량을 서로 다른 목록으로 보여준다.
        response.put("resident_cars", residentVehicleRepository.findByResident_No(residentNo)
                .stream()
                .map(this::toResidentCarMap)
                .toList());
        response.put("visitor_cars", registeredCarRepository.findByResident_No(residentNo)
                .stream()
                .map(this::toVisitorCarMap)
                .toList());
        return response;
    }

    @Transactional
    public Map<String, Object> create(Integer residentNo, AppCarSaveRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car number is required.");
        }

        ResidentEntity resident = residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));

        // 방문 차량은 앱 전용 registered_cars 테이블에 저장한다.
        if (isVisitorCar(requestDto.getCarType())) {
            validateVisitorCarLimit(resident);
            RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                    .resident(resident)
                    .number(requestDto.getNumber().trim())
                    .build();
            registeredCarRepository.save(visitorCar);
            return success();
        }

         // 입주민 차량은 웹 관리자와 공유하는 car 테이블에 저장한다.
        validateResidentCarLimit(resident);
        ResidentVehicleEntity residentVehicle = ResidentVehicleEntity.builder()
                .resident(resident)
                .number(requestDto.getNumber().trim())
                // 👇 [수정 전]
                // .name(null)
                // .kind(trimToNull(requestDto.getName()))
                // 👇 [수정 후: 제자리 찾아주기!]
                .name(trimToNull(requestDto.getName()))
                .kind(trimToNull(requestDto.getCarType()))
                .note(trimToNull(requestDto.getNote()))
                .build();
        residentVehicleRepository.save(residentVehicle);
        return success();
    }

@Transactional
    public Map<String, Object> delete(Integer residentNo, String carNumber) {
        // 입주민 차량 삭제 시도
        long deletedResidentCars = residentVehicleRepository.deleteByNumberAndResident_No(carNumber, residentNo);
        if (deletedResidentCars > 0) {
            return success();
        }

        // 방문 차량 삭제 시도
        long deletedVisitorCars = registeredCarRepository.deleteByNumberAndResident_No(carNumber, residentNo);
        if (deletedVisitorCars > 0) {
            return success();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found.");
    }

    private Map<String, Object> toResidentCarMap(ResidentVehicleEntity vehicle) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("c_no", vehicle.getNo());
        item.put("c_number", vehicle.getNumber());
        item.put("c_kind", vehicle.getKind());
        item.put("c_note", vehicle.getNote());
        item.put("c_date", vehicle.getRegisteredAt());
        item.put("u_no", vehicle.getResident() != null ? vehicle.getResident().getNo() : null);
        item.put("c_name", vehicle.getName() != null ? vehicle.getName() : vehicle.getResident().getName() + " \uCC28\uB7C9");
        return item;
    }

    private Map<String, Object> toVisitorCarMap(RegisteredCarEntity vehicle) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("v_no", vehicle.getNo());
        item.put("u_no", vehicle.getResident() != null ? vehicle.getResident().getNo() : null);
        item.put("c_number", vehicle.getNumber());
        item.put("reg_time", vehicle.getRegisteredAt());
        item.put("park_time", vehicle.getParkedAt());
        item.put("expire_date", vehicle.getExpiresAt());
        return item;
    }

    private boolean isVisitorCar(String carType) {
        // Flutter에서 넘어오는 한글/영문 차량 타입을 모두 허용한다.
        return carType != null && (carType.contains("\uBC29\uBB38") || carType.toLowerCase().contains("visitor"));
    }

    private void validateResidentCarLimit(ResidentEntity resident) {
        int limit = resident.getResidentCarLimit() != null ? resident.getResidentCarLimit() : 1;
        long currentCount = residentVehicleRepository.countByResident_No(resident.getNo());
        if (currentCount >= limit) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "입주민 차량 등록 가능 대수를 초과했습니다.");
        }
    }

    private void validateVisitorCarLimit(ResidentEntity resident) {
        int limit = resident.getVisitorCarLimit() != null ? resident.getVisitorCarLimit() : 2;
        long currentCount = registeredCarRepository.countByResident_No(resident.getNo());
        if (currentCount >= limit) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "방문 차량 등록 가능 대수를 초과했습니다.");
        }
    }

    private Map<String, Object> success() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
