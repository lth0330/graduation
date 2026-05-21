package web.resident.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        ResidentEntity resident = ResidentEntity.builder()
                .apartment(apartment)
                .loginId(requestDto.getLoginId().trim())
                .password(requestDto.getPassword())
                .name(requestDto.getName().trim())
                .email(requestDto.getEmail().trim())
                .dong(requestDto.getBuilding().trim())
                .ho(requestDto.getUnit().trim())
                .phone(requestDto.getPhone() != null ? requestDto.getPhone().trim() : null)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        return toManagementDto(residentRepository.save(resident));
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
                .vehicleCount((int) residentVehicleRepository.countByResident_No(resident.getNo()))
                .joinedAt(resident.getRegisteredAt())
                .approvalStatus(resident.getApprovalStatus())
                .build();
    }
}
