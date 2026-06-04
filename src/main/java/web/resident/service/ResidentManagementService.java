package web.resident.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.common.type.ApprovalStatus;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.dto.ResidentCreateRequestDto;
import web.resident.dto.ResidentManagementDto;
import web.resident.dto.ResidentUpdateRequestDto;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 웹 입주민 관리 서비스: user 테이블의 승인된 입주민 CRUD를 처리한다.
public class ResidentManagementService {

    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordEncoder passwordEncoder;

    public List<ResidentManagementDto> findApprovedResidents(Integer apartmentNo) {
        // Read: 특정 아파트의 승인된 입주민 목록을 조회한다.
        return residentRepository.findByApartment_NoAndApprovalStatus(apartmentNo, ApprovalStatus.APPROVED)
                .stream()
                .map(this::toManagementDto)
                .toList();
    }

    public ResidentManagementDto findResident(Integer residentNo) {
        // Read: 입주민 단건을 조회한다.
        return toManagementDto(findEntity(residentNo));
    }

    @Transactional
    public ResidentManagementDto create(ResidentCreateRequestDto requestDto) {
        // Create: 관리자가 입주민을 승인 상태로 직접 생성한다.
        validateCreateRequest(requestDto);

        if (residentRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 주민 아이디입니다.");
        }

        ApartmentEntity apartment = apartmentRepository.findById(requestDto.getApartmentNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트입니다."));

        Integer residentCarLimit = normalizeLimit(requestDto.getResidentCarLimit(), 1);
        ResidentEntity resident = ResidentEntity.builder()
                .apartment(apartment)
                .loginId(requestDto.getLoginId().trim())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName().trim())
                .email(requestDto.getEmail().trim())
                .dong(requestDto.getBuilding().trim())
                .ho(requestDto.getUnit().trim())
                .phone(requestDto.getPhone() != null ? requestDto.getPhone().trim() : null)
                .approvalStatus(ApprovalStatus.APPROVED)
                .residentCarLimit(residentCarLimit)
                .visitorCarLimit(normalizeLimit(requestDto.getVisitorCarLimit(), 2))
                .build();

        ResidentEntity savedResident = residentRepository.save(resident);
        syncHouseholdResidentCarLimit(savedResident, residentCarLimit);
        return toManagementDto(savedResident);
    }

    @Transactional
    public ResidentManagementDto update(Integer residentNo, ResidentUpdateRequestDto requestDto) {
        // Update: 입주민 연락처와 세대 정보를 수정한다.
        ResidentEntity resident = findEntity(residentNo);

        if (requestDto.getName() != null) {
            resident.setName(requestDto.getName());
        }
        if (requestDto.getEmail() != null) {
            resident.setEmail(requestDto.getEmail());
        }
        if (requestDto.getBuilding() != null) {
            resident.setDong(requestDto.getBuilding());
        }
        if (requestDto.getUnit() != null) {
            resident.setHo(requestDto.getUnit());
        }
        if (requestDto.getPhone() != null) {
            resident.setPhone(requestDto.getPhone());
        }
        Integer residentCarLimit = requestDto.getResidentCarLimit() != null
                ? normalizeLimit(requestDto.getResidentCarLimit(), 1)
                : findHouseholdResidentCarLimit(resident);
        resident.setResidentCarLimit(residentCarLimit);
        if (requestDto.getVisitorCarLimit() != null) {
            resident.setVisitorCarLimit(normalizeLimit(requestDto.getVisitorCarLimit(), 2));
        }

        syncHouseholdResidentCarLimit(resident, residentCarLimit);
        return toManagementDto(resident);
    }

    @Transactional
    public void delete(Integer residentNo) {
        // Delete: 입주민과 연결된 차량을 먼저 삭제한 뒤 입주민을 삭제한다.
        ResidentEntity resident = findEntity(residentNo);
        List<ResidentVehicleEntity> vehicles = residentVehicleRepository.findByResident_No(residentNo);
        residentVehicleRepository.deleteAll(vehicles);
        residentRepository.delete(resident);
    }

    private ResidentEntity findEntity(Integer residentNo) {
        return residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주민입니다."));
    }

    private void validateCreateRequest(ResidentCreateRequestDto requestDto) {
        if (requestDto.getApartmentNo() == null
                || isBlank(requestDto.getLoginId())
                || isBlank(requestDto.getPassword())
                || isBlank(requestDto.getName())
                || isBlank(requestDto.getEmail())
                || isBlank(requestDto.getBuilding())
                || isBlank(requestDto.getUnit())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주민 등록 필수값을 입력하세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Integer normalizeLimit(Integer limit, int defaultLimit) {
        if (limit == null) {
            return defaultLimit;
        }
        if (limit < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차량 등록 제한 수는 0 이상이어야 합니다.");
        }
        return limit;
    }

    private Integer getResidentCarLimit(ResidentEntity resident) {
        return resident.getResidentCarLimit() != null ? resident.getResidentCarLimit() : 1;
    }

    private Integer getVisitorCarLimit(ResidentEntity resident) {
        return resident.getVisitorCarLimit() != null ? resident.getVisitorCarLimit() : 2;
    }

    private ResidentManagementDto toManagementDto(ResidentEntity resident) {
        return ResidentManagementDto.builder()
                .residentNo(resident.getNo())
                .apartmentNo(resident.getApartment() != null ? resident.getApartment().getNo() : null)
                .name(resident.getName())
                .loginId(resident.getLoginId())
                .email(resident.getEmail())
                .building(resident.getDong())
                .unit(resident.getHo())
                .phone(resident.getPhone())
                .vehicleCount(countHouseholdResidentVehicles(resident))
                .residentCarLimit(getResidentCarLimit(resident))
                .visitorCarLimit(getVisitorCarLimit(resident))
                .joinedAt(resident.getRegisteredAt())
                .approvalStatus(resident.getApprovalStatus())
                .build();
    }

    private int countHouseholdResidentVehicles(ResidentEntity resident) {
        Integer apartmentNo = resident.getApartment() != null ? resident.getApartment().getNo() : null;
        if (apartmentNo == null || isBlank(resident.getDong()) || isBlank(resident.getHo())) {
            return 0;
        }

        return (int) residentVehicleRepository.countByResident_Apartment_NoAndResident_DongAndResident_Ho(
                apartmentNo,
                resident.getDong(),
                resident.getHo()
        );
    }

    private Integer findHouseholdResidentCarLimit(ResidentEntity resident) {
        Integer apartmentNo = resident.getApartment() != null ? resident.getApartment().getNo() : null;
        if (apartmentNo == null || isBlank(resident.getDong()) || isBlank(resident.getHo())) {
            return getResidentCarLimit(resident);
        }

        return residentRepository.findByApartment_NoAndDongAndHo(apartmentNo, resident.getDong(), resident.getHo())
                .stream()
                .filter(householdResident -> !householdResident.getNo().equals(resident.getNo()))
                .filter(householdResident -> householdResident.getApprovalStatus() == ApprovalStatus.APPROVED)
                .map(ResidentEntity::getResidentCarLimit)
                .filter(limit -> limit != null && limit >= 0)
                .findFirst()
                .orElseGet(() -> residentRepository.findByApartment_NoAndDongAndHo(apartmentNo, resident.getDong(), resident.getHo())
                        .stream()
                        .filter(householdResident -> !householdResident.getNo().equals(resident.getNo()))
                        .map(ResidentEntity::getResidentCarLimit)
                        .filter(limit -> limit != null && limit >= 0)
                        .findFirst()
                        .orElseGet(() -> getResidentCarLimit(resident)));
    }

    private void syncHouseholdResidentCarLimit(ResidentEntity resident, Integer residentCarLimit) {
        Integer apartmentNo = resident.getApartment() != null ? resident.getApartment().getNo() : null;
        if (apartmentNo == null || isBlank(resident.getDong()) || isBlank(resident.getHo())) {
            resident.setResidentCarLimit(residentCarLimit);
            return;
        }

        residentRepository.findByApartment_NoAndDongAndHo(apartmentNo, resident.getDong(), resident.getHo())
                .forEach(householdResident -> householdResident.setResidentCarLimit(residentCarLimit));
    }
}
