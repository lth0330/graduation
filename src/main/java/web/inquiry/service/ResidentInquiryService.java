package web.inquiry.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.inquiry.dto.ResidentInquiryAnswerRequestDto;
import web.inquiry.dto.ResidentInquiryCreateRequestDto;
import web.inquiry.dto.ResidentInquiryDto;
import web.inquiry.entity.ResidentInquiryEntity;
import web.inquiry.repository.ResidentInquiryRepository;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResidentInquiryService {

    private final ResidentInquiryRepository residentInquiryRepository;
    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ApartmentManagerRepository apartmentManagerRepository;

    @Transactional
    public ResidentInquiryDto create(ResidentInquiryCreateRequestDto requestDto) {
        validateCreateRequest(requestDto);

        ResidentEntity resident = findResident(requestDto.getResidentNo());
        ResidentVehicleEntity vehicle = null;
        if (requestDto.getVehicleNo() != null) {
            vehicle = findVehicle(requestDto.getVehicleNo());
            if (vehicle.getResident() == null || !vehicle.getResident().getNo().equals(resident.getNo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주민과 차량 정보가 일치하지 않습니다.");
            }
        }

        ResidentInquiryEntity inquiry = ResidentInquiryEntity.builder()
                .resident(resident)
                .vehicle(vehicle)
                .title(requestDto.getTitle().trim())
                .content(requestDto.getContent().trim())
                .status("pending")
                .build();

        return toDto(residentInquiryRepository.save(inquiry));
    }

    public List<ResidentInquiryDto> findByApartment(Map<String, Object> principal, Integer apartmentNo) {
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        Integer targetApartmentNo = apartmentNo != null ? apartmentNo : getApartmentNo(manager);

        if (!targetApartmentNo.equals(getApartmentNo(manager))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 아파트의 문의는 조회할 수 없습니다.");
        }

        return residentInquiryRepository.findByResident_Apartment_NoOrderByCreatedAtDesc(targetApartmentNo)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ResidentInquiryDto findByNo(Map<String, Object> principal, Integer inquiryNo) {
        ResidentInquiryEntity inquiry = findEntity(inquiryNo);
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        validateManagerApartment(manager, inquiry);
        return toDto(inquiry);
    }

    @Transactional
    public ResidentInquiryDto answer(
            Map<String, Object> principal,
            Integer inquiryNo,
            ResidentInquiryAnswerRequestDto requestDto
    ) {
        if (requestDto == null || isBlank(requestDto.getAnswer())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변 내용을 입력하세요.");
        }

        ResidentInquiryEntity inquiry = findEntity(inquiryNo);
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        validateManagerApartment(manager, inquiry);

        inquiry.setAnswer(requestDto.getAnswer().trim());
        inquiry.setStatus("answered");
        inquiry.setAnsweredAt(LocalDateTime.now());
        return toDto(inquiry);
    }

    private void validateCreateRequest(ResidentInquiryCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 정보를 입력하세요.");
        }
        if (requestDto.getResidentNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주민 번호는 필수입니다.");
        }
        if (isBlank(requestDto.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 제목은 필수입니다.");
        }
        if (isBlank(requestDto.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 내용은 필수입니다.");
        }
    }

    private ResidentInquiryEntity findEntity(Integer inquiryNo) {
        return residentInquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주민 문의입니다."));
    }

    private ResidentEntity findResident(Integer residentNo) {
        return residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주민입니다."));
    }

    private ResidentVehicleEntity findVehicle(Integer vehicleNo) {
        return residentVehicleRepository.findById(vehicleNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 차량입니다."));
    }

    private ApartmentManagerEntity findManager(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));
    }

    private void validateManagerApartment(ApartmentManagerEntity manager, ResidentInquiryEntity inquiry) {
        ResidentEntity resident = inquiry.getResident();
        ApartmentEntity apartment = resident != null ? resident.getApartment() : null;

        if (apartment == null || !apartment.getNo().equals(getApartmentNo(manager))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 아파트의 문의는 처리할 수 없습니다.");
        }
    }

    private Integer getApartmentNo(ApartmentManagerEntity manager) {
        ApartmentEntity apartment = manager != null ? manager.getApartment() : null;

        if (apartment == null || apartment.getNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 아파트 정보가 없습니다.");
        }

        return apartment.getNo();
    }

    private ResidentInquiryDto toDto(ResidentInquiryEntity inquiry) {
        ResidentEntity resident = inquiry.getResident();
        ApartmentEntity apartment = resident != null ? resident.getApartment() : null;
        ResidentVehicleEntity vehicle = inquiry.getVehicle();

        return ResidentInquiryDto.builder()
                .inquiryNo(inquiry.getNo())
                .residentNo(resident != null ? resident.getNo() : null)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .writer(resident != null ? resident.getName() : null)
                .building(resident != null ? resident.getDong() : null)
                .unit(resident != null ? resident.getHo() : null)
                .vehicleNo(vehicle != null ? vehicle.getNo() : null)
                .carNumber(vehicle != null ? vehicle.getNumber() : null)
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .status(inquiry.getStatus())
                .answer(inquiry.getAnswer())
                .createdAt(inquiry.getCreatedAt())
                .answeredAt(inquiry.getAnsweredAt())
                .build();
    }

    private Integer getInteger(Map<String, Object> principal, String key) {
        Object value = principal != null ? principal.get(key) : null;

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
