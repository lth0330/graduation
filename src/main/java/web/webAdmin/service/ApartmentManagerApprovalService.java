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
// 아파트 관리자 가입승인 서비스: 승인/거절 상태 변경과 알림 메일 발송을 처리한다.
public class ApartmentManagerApprovalService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final GmailMailService gmailMailService;

    public List<ApartmentManagerSignupListDto> findSignupRequests() {
        // Read: 아파트 관리자 가입 요청 목록을 조회한다.
        return apartmentManagerRepository.findAll()
                .stream()
                .map(ApartmentManagerEntity::toSignupListDTO)
                .toList();
    }

    public ApartmentManagerSignupListDto findSignupRequest(Integer managerNo) {
        // Read: 가입 요청 1건을 조회한다.
        return findEntity(managerNo).toSignupListDTO();
    }

    @Transactional
    public ApartmentManagerSignupListDto approve(Integer managerNo) {
        // Update: 가입 요청을 승인 상태로 변경한다.
        ApartmentManagerEntity manager = findEntity(managerNo);
        validatePending(manager);
        manager.setApprovalStatus(ApprovalStatus.APPROVED);
        manager.setRejectReason(null);
        manager.setApprovedAt(LocalDateTime.now());
        gmailMailService.sendApprovalMail(manager.getEmail(), manager.getName(), "아파트 관리자 회원가입");
        return manager.toSignupListDTO();
    }

    @Transactional
    public ApartmentManagerSignupListDto reject(Integer managerNo, String rejectReason) {
        // Update: 가입 요청을 거절 상태로 변경하고 거절 사유를 저장한다.
        if (isBlank(rejectReason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 사유를 입력해주세요.");
        }

        ApartmentManagerEntity manager = findEntity(managerNo);
        validatePending(manager);
        String normalizedRejectReason = rejectReason.trim();
        manager.setApprovalStatus(ApprovalStatus.REJECTED);
        manager.setRejectReason(normalizedRejectReason);
        manager.setApprovedAt(null);
        gmailMailService.sendRejectMail(manager.getEmail(), manager.getName(), "아파트 관리자 회원가입", normalizedRejectReason);
        return manager.toSignupListDTO();
    }

    private ApartmentManagerEntity findEntity(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자 신청입니다."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validatePending(ApartmentManagerEntity manager) {
        if (manager.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 아파트 관리자 신청입니다.");
        }
    }
}
