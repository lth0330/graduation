package app.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import app.dto.AppCarSaveRequestDto;
import app.service.AppVehicleService;

@RestController
@RequiredArgsConstructor
// 앱 차량 관리 컨트롤러: 입주민 차량과 방문 차량의 조회/등록/삭제를 담당한다.
public class AppVehicleController {

    private final AppVehicleService appVehicleService;

    @GetMapping("/api/cars")
    // Read: 로그인한 입주민의 차량 목록을 조회한다.
    public ResponseEntity<Map<String, Object>> findCars(Authentication authentication) {
        return ResponseEntity.ok(appVehicleService.findCars(getUserNo(authentication)));
    }

    @PostMapping("/api/cars")
    // Create: 입주민 차량 또는 방문 차량을 등록한다.
    public ResponseEntity<Map<String, Object>> create(
            Authentication authentication,
            @RequestBody AppCarSaveRequestDto requestDto
    ) {
        return ResponseEntity.ok(appVehicleService.create(getUserNo(authentication), requestDto));
    }

    @DeleteMapping("/api/cars/{carNumber}")
    // Delete: 차량번호 기준으로 입주민 차량 또는 방문 차량을 삭제한다.
    public ResponseEntity<Map<String, Object>> delete(
            Authentication authentication,
            @PathVariable String carNumber
    ) {
        return ResponseEntity.ok(appVehicleService.delete(getUserNo(authentication), carNumber));
    }

    private Integer getUserNo(Authentication authentication) {
        Map<?, ?> principal = (Map<?, ?>) authentication.getPrincipal();
        Object userNo = principal.get("userNo");
        return userNo instanceof Integer integerUserNo ? integerUserNo : Integer.valueOf(userNo.toString());
    }
}
