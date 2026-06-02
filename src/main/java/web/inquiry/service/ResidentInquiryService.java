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
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;
import app.entity.AppNotificationEntity;
import app.repository.AppNotificationRepository;
import app.repository.DeviceInfoRepository;
import app.service.FcmService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 입주민 문의 서비스: 입주민 문의 등록/조회와 아파트 관리자 답변 수정을 처리한다.
public class ResidentInquiryService {

    private final ResidentInquiryRepository residentInquiryRepository;
    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ManagerNotificationService managerNotificationService;
    // 👇👇 [여기부터 새로 추가] 👇👇
    private final AppNotificationRepository appNotificationRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final FcmService fcmService;
    // 👆👆 [여기까지 추가] 👆👆

    @Transactional
    public ResidentInquiryDto create(ResidentInquiryCreateRequestDto requestDto) {
        // Create: 입주민과 차량 정보를 연결해 문의를 등록한다.
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

        ResidentInquiryEntity savedInquiry = residentInquiryRepository.save(inquiry);
        ApartmentEntity apartment = resident.getApartment();
        managerNotificationService.createApartmentNotification(
                apartment,
                "resident_inquiry",
                "새 입주민 문의",
                "입주민 문의가 새로 등록되었습니다.",
                "resident_inquiry",
                savedInquiry.getNo()
        );

        return toDto(savedInquiry);
    }

    public List<ResidentInquiryDto> findByApartment(Map<String, Object> principal, Integer apartmentNo) {
        // Read: 관리자의 아파트 권한을 확인한 뒤 해당 아파트 문의 목록을 조회한다.
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
        // Read: 문의 단건을 조회하고 접근 가능한 아파트인지 확인한다.
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
        // Update: 문의 답변과 처리 상태를 저장한다.
        if (requestDto == null || isBlank(requestDto.getAnswer())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변 내용을 입력하세요.");
        }

        ResidentInquiryEntity inquiry = findEntity(inquiryNo);
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        validateManagerApartment(manager, inquiry);

        inquiry.setAnswer(requestDto.getAnswer().trim());
        inquiry.setStatus("answered");
        inquiry.setAnsweredAt(LocalDateTime.now());
<<<<<<< HEAD

        // =========================================================
        // 👇👇 [새로 추가된 앱 푸시 알림 & DB 저장 로직] 👇👇
        // =========================================================
        ResidentEntity resident = inquiry.getResident();
        if (resident != null) {
            String title = "💬 문의 답변 완료";
            String message = "등록하신 문의 [" + inquiry.getTitle() + "] 에 대한 관리자 답변이 등록되었습니다.";

            // 1. 앱의 알림 보관함(AppNotificationEntity)에 데이터 저장
            appNotificationRepository.save(AppNotificationEntity.builder()
                    .resident(resident)
                    .type("inquiry") // 앱에서 문의 아이콘을 띄우고 문의 탭으로 이동하게 하는 핵심 타입
                    .title(title)
                    .message(message)
                    .read(false) // 안 읽음 상태로 저장
                    .build());

            // 2. 해당 입주민이 로그인한 모든 기기(스마트폰)를 찾아 푸시(FCM) 발송
            deviceInfoRepository.findByResident_No(resident.getNo()).forEach(device -> {
                fcmService.sendPush(device.getFcmToken(), title, message);
            });
        }
        // =========================================================

=======
        managerNotificationService.markReferenceAsRead(
                inquiry.getResident() != null ? inquiry.getResident().getApartment() : null,
                "resident_inquiry",
                inquiry.getNo()
        );
>>>>>>> 127e954a3975603887a4597667b1aa95d2000a2a
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
        ResidentVehicleEntity vehicle = resolveInquiryVehicle(inquiry);

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

    private ResidentVehicleEntity resolveInquiryVehicle(ResidentInquiryEntity inquiry) {
        if (inquiry.getVehicle() != null) {
            return inquiry.getVehicle();
        }
        ResidentEntity resident = inquiry.getResident();
        if (resident == null || resident.getNo() == null) {
            return null;
        }
        return residentVehicleRepository.findByResident_No(resident.getNo())
                .stream()
                .findFirst()
                .orElse(null);
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
