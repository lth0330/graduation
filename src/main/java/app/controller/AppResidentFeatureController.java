package app.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import app.dto.AppDeviceTokenRequestDto;
import app.dto.AppInquiryCreateRequestDto;
import app.dto.AppParkingUpdateRequestDto;
import app.dto.AppSettingRequestDto;
import app.dto.AppVisitorEntryRequestDto;
import app.dto.AppWaitlistRequestDto;
import app.service.AppResidentFeatureService;

@RestController
@RequiredArgsConstructor
// 앱 입주민 부가 기능 컨트롤러: 문의, 알림, 기기 토큰, 설정, 대기 신청, 입차/주차 상태 연동을 담당한다.
public class AppResidentFeatureController {

    private final AppResidentFeatureService appResidentFeatureService;

    @GetMapping("/api/inquiries")
    // Read: 로그인한 입주민의 문의 목록을 조회한다.
    public ResponseEntity<Map<String, Object>> findInquiries(Authentication authentication) {
        return ResponseEntity.ok(appResidentFeatureService.findInquiries(getUserNo(authentication)));
    }

    @PostMapping("/api/inquiries")
    // Create: 입주민 문의를 새로 등록한다.
    public ResponseEntity<Map<String, Object>> createInquiry(
            Authentication authentication,
            @RequestBody AppInquiryCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(appResidentFeatureService.createInquiry(getUserNo(authentication), requestDto));
    }

    @GetMapping("/api/notifications")
    // Read: 로그인한 입주민의 알림 목록을 조회한다.
    public ResponseEntity<Map<String, Object>> findNotifications(Authentication authentication) {
        return ResponseEntity.ok(appResidentFeatureService.findNotifications(getUserNo(authentication)));
    }

    @PatchMapping("/api/notifications/{notificationNo}/read")
    // Update: 선택한 알림을 읽음 상태로 변경한다.
    public ResponseEntity<Map<String, Object>> readNotification(
            Authentication authentication,
            @PathVariable Integer notificationNo
    ) {
        return ResponseEntity.ok(appResidentFeatureService.readNotification(getUserNo(authentication), notificationNo));
    }

    @PostMapping("/api/device-token")
    // Create/Update: 앱 푸시 알림용 FCM 토큰을 저장하거나 갱신한다.
    public ResponseEntity<Map<String, Object>> saveDeviceToken(
            Authentication authentication,
            @RequestBody AppDeviceTokenRequestDto requestDto
    ) {
        return ResponseEntity.ok(appResidentFeatureService.saveDeviceToken(getUserNo(authentication), requestDto));
    }

    @DeleteMapping("/api/device-token")
    // Delete: 저장된 FCM 토큰을 제거해 푸시 수신을 끊는다.
    public ResponseEntity<Map<String, Object>> deleteDeviceToken(Authentication authentication) {
        return ResponseEntity.ok(appResidentFeatureService.deleteDeviceToken(getUserNo(authentication)));
    }

    @PatchMapping("/api/settings/push")
    // Update: 앱 푸시 알림 설정을 변경한다.
    public ResponseEntity<Map<String, Object>> updatePushSetting(
            Authentication authentication,
            @RequestBody AppSettingRequestDto requestDto
    ) {
        return ResponseEntity.ok(appResidentFeatureService.updatePushSetting(getUserNo(authentication), requestDto));
    }

    @PatchMapping("/api/settings/theme")
    // Update: 앱 테마 설정을 변경한다.
    public ResponseEntity<Map<String, Object>> updateThemeSetting(
            Authentication authentication,
            @RequestBody AppSettingRequestDto requestDto
    ) {
        return ResponseEntity.ok(appResidentFeatureService.updateThemeSetting(getUserNo(authentication), requestDto));
    }

    @PostMapping("/api/waitlist")
    // Create: 원하는 주차 구역에 대한 대기 신청을 등록한다.
    public ResponseEntity<Map<String, Object>> createWaitlist(
            Authentication authentication,
            @RequestBody AppWaitlistRequestDto requestDto
    ) {
        return ResponseEntity.ok(appResidentFeatureService.createWaitlist(getUserNo(authentication), requestDto));
    }

    @PostMapping("/api/visitor-entry")
    // Update/Create: 방문 차량 입차 시간을 기록하고 알림을 생성한다.
    public ResponseEntity<Map<String, Object>> visitorEntry(@RequestBody AppVisitorEntryRequestDto requestDto) {
        return ResponseEntity.ok(appResidentFeatureService.visitorEntry(requestDto));
    }

    @PostMapping("/api/parking-update")
    // Update: 외부 주차 인식 결과를 주차 구역 상태에 반영한다.
    public ResponseEntity<Map<String, Object>> updateParking(@RequestBody AppParkingUpdateRequestDto requestDto) {
        return ResponseEntity.ok(appResidentFeatureService.updateParking(requestDto));
    }

    private Integer getUserNo(Authentication authentication) {
        Map<?, ?> principal = (Map<?, ?>) authentication.getPrincipal();
        Object userNo = principal.get("userNo");
        return userNo instanceof Integer integerUserNo ? integerUserNo : Integer.valueOf(userNo.toString());
    }
}
