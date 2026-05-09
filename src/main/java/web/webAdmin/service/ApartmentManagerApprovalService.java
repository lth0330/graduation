package web.webAdmin.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.common.mail.GmailMailService;
import web.common.type.ApprovalStatus;
import web.webAdmin.dto.ApartmentManagerSignupListDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApartmentManagerApprovalService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final GmailMailService gmailMailService;

    public List<ApartmentManagerSignupListDto> findSignupRequests() {
        return apartmentManagerRepository.findAll()
                .stream()
                .map(ApartmentManagerEntity::toSignupListDTO)
                .toList();
    }

    public ApartmentManagerSignupListDto findSignupRequest(Integer managerNo) {
        return findEntity(managerNo).toSignupListDTO();
    }

    @Transactional
    public ApartmentManagerSignupListDto approve(Integer managerNo) {
        ApartmentManagerEntity manager = findEntity(managerNo);
        manager.setApprovalStatus(ApprovalStatus.APPROVED);
        manager.setRejectReason(null);
        manager.setApprovedAt(LocalDateTime.now());
        gmailMailService.sendApprovalMail(manager.getEmail(), manager.getName(), "아파트 관리자 회원가입");
        return manager.toSignupListDTO();
    }

    @Transactional
    public ApartmentManagerSignupListDto reject(Integer managerNo, String rejectReason) {
        if (isBlank(rejectReason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 사유를 입력해주세요.");
        }

        ApartmentManagerEntity manager = findEntity(managerNo);
        manager.setApprovalStatus(ApprovalStatus.REJECTED);
        manager.setRejectReason(rejectReason);
        manager.setApprovedAt(null);
        gmailMailService.sendRejectMail(manager.getEmail(), manager.getName(), "아파트 관리자 회원가입", rejectReason);
        return manager.toSignupListDTO();
    }

    private ApartmentManagerEntity findEntity(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자 신청입니다."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
