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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.inquiry.dto.ResidentInquiryAnswerRequestDto;
import web.inquiry.dto.ResidentInquiryCreateRequestDto;
import web.inquiry.dto.ResidentInquiryDto;
import web.inquiry.service.ResidentInquiryService;

@RestController
@RequiredArgsConstructor
// 입주민 문의 컨트롤러: 입주민 문의 등록, 조회, 관리자 답변을 담당한다.
public class ResidentInquiryController {

    private final ResidentInquiryService residentInquiryService;

    @PostMapping("/api/resident-inquiries")
    // Create: 입주민 문의를 등록한다.
    public ResponseEntity<ResidentInquiryDto> create(@RequestBody ResidentInquiryCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(residentInquiryService.create(requestDto));
    }

    @GetMapping("/api/resident-inquiries")
    // Read: 특정 아파트의 입주민 문의 목록을 조회한다.
    public ResponseEntity<List<ResidentInquiryDto>> findByApartment(
            @AuthenticationPrincipal Map<String, Object> principal,
            @RequestParam(required = false) Integer apartmentNo
    ) {
        return ResponseEntity.ok(residentInquiryService.findByApartment(principal, apartmentNo));
    }

    @GetMapping("/api/resident-inquiries/{inquiryNo}")
    // Read: 입주민 문의 상세를 조회한다.
    public ResponseEntity<ResidentInquiryDto> findByNo(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer inquiryNo
    ) {
        return ResponseEntity.ok(residentInquiryService.findByNo(principal, inquiryNo));
    }

    @PatchMapping("/api/resident-inquiries/{inquiryNo}/answer")
    // Update: 아파트 관리자가 입주민 문의에 답변한다.
    public ResponseEntity<ResidentInquiryDto> answer(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer inquiryNo,
            @RequestBody ResidentInquiryAnswerRequestDto requestDto
    ) {
        return ResponseEntity.ok(residentInquiryService.answer(principal, inquiryNo, requestDto));
    }
}
