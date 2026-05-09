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
public class ResidentApprovalService {

    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final GmailMailService gmailMailService;

    public List<ResidentApprovalDto> findSignupRequests(Integer apartmentNo) {
        return residentRepository.findByApartment_No(apartmentNo)
                .stream()
                .map(this::toApprovalDto)
                .toList();
    }

    public ResidentApprovalDto findSignupRequest(Integer residentNo) {
        return toApprovalDto(findEntity(residentNo));
    }

    @Transactional
    public ResidentApprovalDto approve(Integer residentNo) {
        ResidentEntity resident = findEntity(residentNo);
        resident.setApprovalStatus(ApprovalStatus.APPROVED);
        resident.setRejectReason(null);
        gmailMailService.sendApprovalMail(resident.getEmail(), resident.getName(), "입주민 회원가입");
        return toApprovalDto(resident);
    }

    @Transactional
    public ResidentApprovalDto reject(Integer residentNo, String rejectReason) {
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
