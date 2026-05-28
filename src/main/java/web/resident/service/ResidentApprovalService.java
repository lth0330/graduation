package web.resident.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.common.mail.GmailMailService;
import web.common.type.ApprovalStatus;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.dto.ResidentApprovalDto;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 입주민 가입승인 서비스: 입주민 가입 요청의 조회, 승인, 거절을 처리한다.
public class ResidentApprovalService {

    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final GmailMailService gmailMailService;

    public List<ResidentApprovalDto> findSignupRequests(Integer apartmentNo) {
        // Read: 특정 아파트의 승인 대기 중인 입주민 가입 요청 목록만 조회한다.
        return residentRepository.findByApartment_NoAndApprovalStatus(apartmentNo, ApprovalStatus.PENDING)
                .stream()
                .map(this::toApprovalDto)
                .toList();
    }

    public ResidentApprovalDto findSignupRequest(Integer residentNo) {
        // Read: 입주민 가입 요청 단건을 조회한다.
        return toApprovalDto(findEntity(residentNo));
    }

    @Transactional
    public ResidentApprovalDto approve(Integer residentNo) {
        // Update: 입주민 가입 요청을 승인 상태로 변경한다.
        ResidentEntity resident = findEntity(residentNo);
        resident.setApprovalStatus(ApprovalStatus.APPROVED);
        resident.setRejectReason(null);
        gmailMailService.sendApprovalMail(resident.getEmail(), resident.getName(), "입주민 회원가입");
        return toApprovalDto(resident);
    }

    @Transactional
    public ResidentApprovalDto reject(Integer residentNo, String rejectReason) {
        // Update: 입주민 가입 요청을 거절 상태로 변경하고 사유를 저장한다.
        if (isBlank(rejectReason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 사유를 입력해주세요.");
        }

        ResidentEntity resident = findEntity(residentNo);
        resident.setApprovalStatus(ApprovalStatus.REJECTED);
        resident.setRejectReason(rejectReason);
        gmailMailService.sendRejectMail(resident.getEmail(), resident.getName(), "입주민 회원가입", rejectReason);
        return toApprovalDto(resident);
    }

    private ResidentEntity findEntity(Integer residentNo) {
        return residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주민 신청입니다."));
    }

    private ResidentApprovalDto toApprovalDto(ResidentEntity resident) {
        ResidentVehicleEntity vehicle = residentVehicleRepository.findByResident_No(resident.getNo())
                .stream()
                .findFirst()
                .orElse(null);

        return ResidentApprovalDto.builder()
                .residentNo(resident.getNo())
                .apartmentNo(resident.getApartment() != null ? resident.getApartment().getNo() : null)
                .name(resident.getName())
                .loginId(resident.getLoginId())
                .email(resident.getEmail())
                .building(resident.getDong())
                .unit(resident.getHo())
                .carNumber(vehicle != null ? vehicle.getNumber() : null)
                .carType(vehicle != null ? vehicle.getKind() : null)
                .approvalStatus(resident.getApprovalStatus())
                .rejectReason(resident.getRejectReason())
                .requestedAt(resident.getRegisteredAt())
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
