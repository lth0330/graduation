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
public class ApartmentManagerApprovalController {

    private final ApartmentManagerApprovalService apartmentManagerApprovalService;

    @GetMapping
    public ResponseEntity<List<ApartmentManagerSignupListDto>> findSignupRequests() {
        return ResponseEntity.ok(apartmentManagerApprovalService.findSignupRequests());
    }

    @GetMapping("/{managerNo}")
    public ResponseEntity<ApartmentManagerSignupListDto> findSignupRequest(@PathVariable Integer managerNo) {
        return ResponseEntity.ok(apartmentManagerApprovalService.findSignupRequest(managerNo));
    }

    @PatchMapping("/{managerNo}/approve")
    public ResponseEntity<ApartmentManagerSignupListDto> approve(@PathVariable Integer managerNo) {
        return ResponseEntity.ok(apartmentManagerApprovalService.approve(managerNo));
    }

    @PatchMapping("/{managerNo}/reject")
    public ResponseEntity<ApartmentManagerSignupListDto> reject(
            @PathVariable Integer managerNo,
            @RequestBody ApartmentManagerRejectRequestDto requestDto
    ) {
        return ResponseEntity.ok(apartmentManagerApprovalService.reject(managerNo, requestDto.getRejectReason()));
    }
}
