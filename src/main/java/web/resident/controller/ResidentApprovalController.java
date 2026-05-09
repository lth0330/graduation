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
public class ResidentApprovalController {

    private final ResidentApprovalService residentApprovalService;

    @GetMapping
    public ResponseEntity<List<ResidentApprovalDto>> findSignupRequests(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(residentApprovalService.findSignupRequests(apartmentNo));
    }

    @GetMapping("/{residentNo}")
    public ResponseEntity<ResidentApprovalDto> findSignupRequest(@PathVariable Integer residentNo) {
        return ResponseEntity.ok(residentApprovalService.findSignupRequest(residentNo));
    }

    @PatchMapping("/{residentNo}/approve")
    public ResponseEntity<ResidentApprovalDto> approve(@PathVariable Integer residentNo) {
        return ResponseEntity.ok(residentApprovalService.approve(residentNo));
    }

    @PatchMapping("/{residentNo}/reject")
    public ResponseEntity<ResidentApprovalDto> reject(
            @PathVariable Integer residentNo,
            @RequestBody ResidentRejectRequestDto requestDto
    ) {
        return ResponseEntity.ok(residentApprovalService.reject(residentNo, requestDto.getRejectReason()));
    }
}
