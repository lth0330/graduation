package app.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import app.service.AppApartmentService;

@RestController
@RequiredArgsConstructor
// 앱 회원가입 화면에서 사용할 아파트 목록 조회 컨트롤러다.
public class AppApartmentController {

    private final AppApartmentService appApartmentService;

    @GetMapping("/api/apartments")
    // Read: 가입 가능한 아파트 목록을 조회한다.
    public ResponseEntity<Map<String, Object>> findApartments() {
        return ResponseEntity.ok(appApartmentService.findApartments());
    }
}
