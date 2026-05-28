package web.parking.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.parking.dto.VisitorCarManagementDto;
import web.parking.service.VisitorCarManagementService;

@RestController
@RequestMapping("/api/visitor-cars")
@RequiredArgsConstructor
// 아파트 관리자 화면에서 방문 차량 목록을 확인하는 컨트롤러다.
public class VisitorCarManagementController {

    private final VisitorCarManagementService visitorCarManagementService;

    @GetMapping
    // Read: 특정 아파트의 방문 차량 목록을 조회한다.
    public ResponseEntity<List<VisitorCarManagementDto>> findVisitorCars(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(visitorCarManagementService.findVisitorCars(apartmentNo));
    }
}
