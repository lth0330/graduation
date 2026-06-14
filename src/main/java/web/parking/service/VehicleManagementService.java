package web.parking.service;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.common.type.ApprovalStatus;
import web.inquiry.repository.ResidentInquiryRepository;
import web.parking.dto.VehicleManagementDto;
import web.parking.dto.VehicleOwnerDto;
import web.parking.dto.VehicleSaveRequestDto;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 웹 차량 관리 서비스: car 테이블의 입주민 차량 CRUD를 처리한다.
public class VehicleManagementService {

    private final ResidentVehicleRepository residentVehicleRepository;
    private final ResidentRepository residentRepository;
    private final ResidentInquiryRepository residentInquiryRepository;
    private final RegisteredCarRepository registeredCarRepository;

    public List<VehicleManagementDto> findVehicles(Integer apartmentNo) {
        // Read: 아파트 번호로 차량 목록을 조회한다.
        return residentVehicleRepository.findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(apartmentNo)
                .stream()
                .map(this::toManagementDto)
                .toList();
    }

    public VehicleManagementDto findVehicle(Integer vehicleNo) {
        // Read: 차량 단건을 조회한다.
        return toManagementDto(findEntity(vehicleNo));
    }

    public VehicleOwnerDto findOwnerByCarNumber(Map<String, Object> principal, String carNumber) {
        String compactNumber = compactCarNumber(carNumber);
        if (compactNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차량번호는 필수입니다.");
        }

        Optional<ResidentVehicleEntity> residentVehicle =
                residentVehicleRepository.findFirstByCompactNumber(compactNumber);
        if (residentVehicle.isPresent()) {
            return toOwnerDto(principal, residentVehicle.get().getResident(), residentVehicle.get().getNumber(), "resident");
        }

        RegisteredCarEntity visitorCar = registeredCarRepository.findFirstByCompactNumber(compactNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "등록된 차량 소유 주민을 찾을 수 없습니다."));
        return toOwnerDto(principal, visitorCar.getResident(), visitorCar.getNumber(), "visitor");
    }

    @Transactional
    public VehicleManagementDto create(VehicleSaveRequestDto requestDto) {
        // Create: 승인된 입주민에 연결된 차량을 등록한다.
        validateCreateRequest(requestDto);

        if (residentVehicleRepository.existsByNumber(requestDto.getCarNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 차량번호입니다.");
        }

        ResidentEntity resident = findApprovedResident(requestDto.getOwnerId());
        validateHouseholdResidentCarLimit(resident);
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .name(resident.getName() + " 차량")
                .number(requestDto.getCarNumber())
                .kind(requestDto.getCarType())
                .note(requestDto.getNote())
                .resident(resident)
                .build();

        return toManagementDto(residentVehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleManagementDto update(Integer vehicleNo, VehicleSaveRequestDto requestDto) {
        // Update: 차량 소유자는 유지하고 차량 기본 정보만 수정한다.
        validateUpdateRequest(requestDto);

        ResidentVehicleEntity vehicle = findEntity(vehicleNo);
        if (residentVehicleRepository.existsByNumberAndNoNot(requestDto.getCarNumber(), vehicleNo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 차량번호입니다.");
        }

        vehicle.setNumber(requestDto.getCarNumber());
        vehicle.setKind(requestDto.getCarType());
        vehicle.setNote(requestDto.getNote());

        return toManagementDto(vehicle);
    }

    @Transactional
    public void delete(Integer vehicleNo) {
        // Delete: 차량 단건을 삭제한다.
        ResidentVehicleEntity vehicle = findEntity(vehicleNo);
        unlinkVehicleFromInquiries(vehicle);
        residentVehicleRepository.delete(vehicle);
    }

    private ResidentVehicleEntity findEntity(Integer vehicleNo) {
        return residentVehicleRepository.findById(vehicleNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 차량입니다."));
    }

    private ResidentEntity findApprovedResident(Integer residentNo) {
        ResidentEntity resident = residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주민입니다."));

        if (resident.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "승인된 주민에게만 차량을 등록할 수 있습니다.");
        }

        return resident;
    }

    private VehicleManagementDto toManagementDto(ResidentVehicleEntity vehicle) {
        ResidentEntity resident = vehicle.getResident();
        return VehicleManagementDto.builder()
                .vehicleNo(vehicle.getNo())
                .carNumber(vehicle.getNumber())
                .carType(vehicle.getKind())
                .ownerId(resident != null ? resident.getNo() : null)
                .ownerName(resident != null ? resident.getName() : null)
                .building(resident != null ? resident.getDong() : null)
                .unit(resident != null ? resident.getHo() : null)
                .note(vehicle.getNote())
                .registeredAt(vehicle.getRegisteredAt())
                .build();
    }

    private VehicleOwnerDto toOwnerDto(
            Map<String, Object> principal,
            ResidentEntity resident,
            String carNumber,
            String vehicleType
    ) {
        if (resident == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "차량 소유 주민을 찾을 수 없습니다.");
        }

        validateSameApartment(principal, resident);
        return VehicleOwnerDto.builder()
                .residentNo(resident.getNo())
                .name(resident.getName())
                .building(resident.getDong())
                .unit(resident.getHo())
                .phone(resident.getPhone())
                .carNumber(carNumber)
                .vehicleType(vehicleType)
                .build();
    }

    private void validateCreateRequest(VehicleSaveRequestDto requestDto) {
        validateVehicleFields(requestDto);
        if (requestDto.getOwnerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "소유 주민은 필수입니다.");
        }
    }

    private void validateUpdateRequest(VehicleSaveRequestDto requestDto) {
        validateVehicleFields(requestDto);
    }

    private void validateVehicleFields(VehicleSaveRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차량 정보를 입력해주세요.");
        }
        if (isBlank(requestDto.getCarNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차량번호는 필수입니다.");
        }
        if (isBlank(requestDto.getCarType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차종은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String compactCarNumber(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").trim();
    }

    private void validateSameApartment(Map<String, Object> principal, ResidentEntity resident) {
        Integer managerApartmentNo = getInteger(principal, "apartmentNo");
        Integer residentApartmentNo = resident.getApartment() != null ? resident.getApartment().getNo() : null;
        if (managerApartmentNo == null || residentApartmentNo == null || !managerApartmentNo.equals(residentApartmentNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 아파트 차량 소유자 정보는 조회할 수 없습니다.");
        }
    }

    private Integer getInteger(Map<String, Object> principal, String key) {
        Object value = principal != null ? principal.get(key) : null;
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Integer.valueOf(stringValue);
        }
        return null;
    }

    private void unlinkVehicleFromInquiries(ResidentVehicleEntity vehicle) {
        residentInquiryRepository.findByVehicle_No(vehicle.getNo())
                .forEach(inquiry -> inquiry.setVehicle(null));
    }

    private void validateHouseholdResidentCarLimit(ResidentEntity resident) {
        Integer apartmentNo = resident.getApartment() != null ? resident.getApartment().getNo() : null;
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

    private int getHouseholdResidentCarLimit(ResidentEntity resident) {
        Integer limit = resident.getResidentCarLimit();
        return limit != null && limit >= 0 ? limit : 1;
    }
}
