package app.controller;

import app.service.AppParkingService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
// 앱 주차 현황 컨트롤러: 앱 화면에 필요한 주차 구역 상태를 조회한다.
public class AppParkingController {

    private final AppParkingService appParkingService;

    @GetMapping("/api/app/parking-zones")
    // Read: 전체 주차 구역과 사용 상태를 앱 형식으로 조회한다.
    public ResponseEntity<Map<String, Object>> findParkingZones() {
        return ResponseEntity.ok(appParkingService.findParkingZones());
    }
}
