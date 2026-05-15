package web.inquiry.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.inquiry.dto.ManagerInquiryAnswerRequestDto;
import web.inquiry.dto.ManagerInquiryCreateRequestDto;
import web.inquiry.dto.ManagerInquiryDto;
import web.inquiry.service.ManagerInquiryService;

@RestController
@RequiredArgsConstructor
public class ManagerInquiryController {

    private final ManagerInquiryService managerInquiryService;

    @PostMapping("/api/manager-inquiries")
    public ResponseEntity<ManagerInquiryDto> create(
            @AuthenticationPrincipal Map<String, Object> principal,
            @RequestBody ManagerInquiryCreateRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerInquiryService.create(principal, requestDto));
    }

    @GetMapping("/api/manager-inquiries/my")
    public ResponseEntity<List<ManagerInquiryDto>> findMine(@AuthenticationPrincipal Map<String, Object> principal) {
        return ResponseEntity.ok(managerInquiryService.findMine(principal));
    }

    @GetMapping("/api/web-admin/inquiries")
    public ResponseEntity<List<ManagerInquiryDto>> findAll() {
        return ResponseEntity.ok(managerInquiryService.findAll());
    }

    @GetMapping("/api/web-admin/inquiries/{inquiryNo}")
    public ResponseEntity<ManagerInquiryDto> findByNo(@PathVariable Integer inquiryNo) {
        return ResponseEntity.ok(managerInquiryService.findByNo(inquiryNo));
    }

    @PatchMapping("/api/web-admin/inquiries/{inquiryNo}/answer")
    public ResponseEntity<ManagerInquiryDto> answer(
            @PathVariable Integer inquiryNo,
            @RequestBody ManagerInquiryAnswerRequestDto requestDto
    ) {
        return ResponseEntity.ok(managerInquiryService.answer(inquiryNo, requestDto));
    }
}
