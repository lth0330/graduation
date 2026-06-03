package app.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import app.dto.AppCarSaveRequestDto;
import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import web.inquiry.repository.ResidentInquiryRepository;
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
    private final ResidentInquiryRepository residentInquiryRepository;

    public Map<String, Object> findCars(Integer residentNo) {
        ResidentEntity resident = findResident(residentNo);
        Map<String, Object> response = success();
        // 앱 화면은 세대 입주민 차량과 개인 방문 차량을 서로 다른 목록으로 보여준다.
        response.put("resident_cars", findHouseholdResidentVehicles(resident)
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

        String carNumber = requestDto.getNumber().trim();
        ResidentEntity resident = findResident(residentNo);

        // 방문 차량은 앱 전용 registered_cars 테이블에 저장한다.
        if (isVisitorCar(requestDto.getCarType())) {
            validateDuplicateCarNumber(carNumber);
            validateVisitorCarLimit(resident);
            RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                    .resident(resident)
                    .number(carNumber)
                    .build();
            registeredCarRepository.save(visitorCar);
            return success();
        }
        // 입주민 차량은 웹 관리자와 공유하는 car 테이블에 저장한다.
        validateDuplicateCarNumber(carNumber);
        validateHouseholdResidentCarLimit(resident);
        ResidentVehicleEntity residentVehicle = ResidentVehicleEntity.builder()
                .resident(resident)
                .number(carNumber)
                .name(resident.getName() + " 차량")
                .kind(trimToNull(requestDto.getName()))
                .note(trimToNull(requestDto.getNote()))
                .build();

        residentVehicleRepository.save(residentVehicle);
        return success();
    }

    @Transactional
    public Map<String, Object> delete(Integer residentNo, String carNumber) {
        ResidentEntity resident = findResident(residentNo);

        // 같은 세대에 등록된 입주민 차량 삭제 시도
        ResidentVehicleEntity residentVehicle = findHouseholdResidentVehicles(resident)
                .stream()
                .filter(vehicle -> carNumber.equals(vehicle.getNumber()))
                .findFirst()
                .orElse(null);
        if (residentVehicle != null) {
            unlinkVehicleFromInquiries(residentVehicle);
            residentVehicleRepository.delete(residentVehicle);
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

    private ResidentEntity findResident(Integer residentNo) {
        return residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));
    }

    private List<ResidentVehicleEntity> findHouseholdResidentVehicles(ResidentEntity resident) {
        Integer apartmentNo = getApartmentNo(resident);
        if (apartmentNo == null || isBlank(resident.getDong()) || isBlank(resident.getHo())) {
            return List.of();
        }

        return residentVehicleRepository.findByResident_Apartment_NoAndResident_DongAndResident_Ho(
                apartmentNo,
                resident.getDong(),
                resident.getHo()
        );
    }

    private void validateHouseholdResidentCarLimit(ResidentEntity resident) {
        Integer apartmentNo = getApartmentNo(resident);
        if (apartmentNo == null || isBlank(resident.getDong()) || isBlank(resident.getHo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주민의 세대 정보가 없어 차량을 등록할 수 없습니다.");
        }

        long currentCount = residentVehicleRepository.countByResident_Apartment_NoAndResident_DongAndResident_Ho(
                apartmentNo,
                resident.getDong(),
                resident.getHo()
        );
        int limit = getHouseholdResidentCarLimit(resident);
        if (currentCount >= limit) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 세대의 입주민 차량 등록 가능 대수를 초과했습니다.");
        }
    }

    private void validateVisitorCarLimit(ResidentEntity resident) {
        int limit = resident.getVisitorCarLimit() != null ? resident.getVisitorCarLimit() : 2;
        long currentCount = registeredCarRepository.countByResident_No(resident.getNo());
        if (currentCount >= limit) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "방문 차량 등록 가능 대수를 초과했습니다.");
        }
    }

    private void validateDuplicateCarNumber(String carNumber) {
        if (residentVehicleRepository.existsByNumber(carNumber) || registeredCarRepository.existsByNumber(carNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 차량번호입니다.");
        }
    }

    private void unlinkVehicleFromInquiries(ResidentVehicleEntity vehicle) {
        residentInquiryRepository.findByVehicle_No(vehicle.getNo())
                .forEach(inquiry -> inquiry.setVehicle(null));
    }

    private int getHouseholdResidentCarLimit(ResidentEntity resident) {
        Integer limit = resident.getResidentCarLimit();
        return limit != null && limit >= 0 ? limit : 1;
    }

    private Integer getApartmentNo(ResidentEntity resident) {
        return resident.getApartment() != null ? resident.getApartment().getNo() : null;
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