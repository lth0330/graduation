package web.webAdmin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.webAdmin.dto.ApartmentManagerRejectRequestDto;
import web.webAdmin.dto.ApartmentManagerSignupListDto;
import web.webAdmin.service.ApartmentManagerApprovalService;

@RestController
@RequestMapping("/api/web-admin/signup-requests")
@RequiredArgsConstructor
// 웹 최고 관리자용 아파트 관리자 가입승인 컨트롤러다.
public class ApartmentManagerApprovalController {

    private final ApartmentManagerApprovalService apartmentManagerApprovalService;

    @GetMapping
    // Read: 아파트 관리자 가입 요청 목록을 조회한다.
    public ResponseEntity<List<ApartmentManagerSignupListDto>> findSignupRequests() {
        return ResponseEntity.ok(apartmentManagerApprovalService.findSignupRequests());
    }

    @GetMapping("/{managerNo}")
    // Read: 특정 아파트 관리자 가입 요청 상세를 조회한다.
    public ResponseEntity<ApartmentManagerSignupListDto> findSignupRequest(@PathVariable Integer managerNo) {
        return ResponseEntity.ok(apartmentManagerApprovalService.findSignupRequest(managerNo));
    }

    @PatchMapping("/{managerNo}/approve")
    // Update: 아파트 관리자 가입 요청을 승인한다.
    public ResponseEntity<ApartmentManagerSignupListDto> approve(@PathVariable Integer managerNo) {
        return ResponseEntity.ok(apartmentManagerApprovalService.approve(managerNo));
    }

    @PatchMapping("/{managerNo}/reject")
    // Update: 아파트 관리자 가입 요청을 거절하고 사유를 저장한다.
    public ResponseEntity<ApartmentManagerSignupListDto> reject(
            @PathVariable Integer managerNo,
            @RequestBody ApartmentManagerRejectRequestDto requestDto
    ) {
        String rejectReason = requestDto != null ? requestDto.getRejectReason() : null;
        return ResponseEntity.ok(apartmentManagerApprovalService.reject(managerNo, rejectReason));
    }
}
