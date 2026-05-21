package web.resident.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.resident.dto.ResidentApprovalDto;
import web.resident.dto.ResidentRejectRequestDto;
import web.resident.service.ResidentApprovalService;

@RestController
@RequestMapping("/api/resident-signup-requests")
@RequiredArgsConstructor
// 아파트 관리자용 입주민 가입승인 컨트롤러다.
public class ResidentApprovalController {

    private final ResidentApprovalService residentApprovalService;

    @GetMapping
    // Read: 특정 아파트의 입주민 가입 요청 목록을 조회한다.
    public ResponseEntity<List<ResidentApprovalDto>> findSignupRequests(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(residentApprovalService.findSignupRequests(apartmentNo));
    }

    @GetMapping("/{residentNo}")
    // Read: 입주민 가입 요청 상세를 조회한다.
    public ResponseEntity<ResidentApprovalDto> findSignupRequest(@PathVariable Integer residentNo) {
        return ResponseEntity.ok(residentApprovalService.findSignupRequest(residentNo));
    }

    @PatchMapping("/{residentNo}/approve")
    // Update: 입주민 가입 요청을 승인한다.
    public ResponseEntity<ResidentApprovalDto> approve(@PathVariable Integer residentNo) {
        return ResponseEntity.ok(residentApprovalService.approve(residentNo));
    }

    @PatchMapping("/{residentNo}/reject")
    // Update: 입주민 가입 요청을 거절하고 사유를 저장한다.
    public ResponseEntity<ResidentApprovalDto> reject(
            @PathVariable Integer residentNo,
            @RequestBody ResidentRejectRequestDto requestDto
    ) {
        return ResponseEntity.ok(residentApprovalService.reject(residentNo, requestDto.getRejectReason()));
    }
}
