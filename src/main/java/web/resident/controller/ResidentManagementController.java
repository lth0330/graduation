package web.resident.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.resident.dto.ResidentContactNotificationRequestDto;
import web.resident.dto.ResidentCreateRequestDto;
import web.resident.dto.ResidentManagementDto;
import web.resident.dto.ResidentUpdateRequestDto;
import web.resident.service.ResidentManagementService;

@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
// 웹 입주민 관리 컨트롤러: 승인된 입주민의 조회, 등록, 수정, 삭제를 담당한다.
public class ResidentManagementController {

    private final ResidentManagementService residentManagementService;

    @GetMapping
    // Read: 특정 아파트의 승인된 입주민 목록을 조회한다.
    public ResponseEntity<List<ResidentManagementDto>> findApprovedResidents(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(residentManagementService.findApprovedResidents(apartmentNo));
    }

    @GetMapping("/{residentNo}")
    // Read: 입주민 번호로 상세 정보를 조회한다.
    public ResponseEntity<ResidentManagementDto> findResident(@PathVariable Integer residentNo) {
        return ResponseEntity.ok(residentManagementService.findResident(residentNo));
    }

    @PostMapping
    // Create: 관리자가 입주민을 직접 등록한다.
    public ResponseEntity<ResidentManagementDto> create(@RequestBody ResidentCreateRequestDto requestDto) {
        return ResponseEntity.ok(residentManagementService.create(requestDto));
    }

    @PostMapping("/{residentNo}/notifications")
    public ResponseEntity<Map<String, Object>> sendContactNotification(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer residentNo,
            @RequestBody ResidentContactNotificationRequestDto requestDto
    ) {
        return ResponseEntity.ok(residentManagementService.sendContactNotification(principal, residentNo, requestDto));
    }

    @PutMapping("/{residentNo}")
    // Update: 입주민 기본 정보를 수정한다.
    public ResponseEntity<ResidentManagementDto> update(
            @PathVariable Integer residentNo,
            @RequestBody ResidentUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(residentManagementService.update(residentNo, requestDto));
    }

    @DeleteMapping("/{residentNo}")
    // Delete: 입주민 계정을 삭제한다.
    public ResponseEntity<Void> delete(@PathVariable Integer residentNo) {
        residentManagementService.delete(residentNo);
        return ResponseEntity.noContent().build();
    }
}
