package python.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import python.dto.PythonGateCheckRequestDto;
import python.service.PythonGateService;

@RestController
@RequiredArgsConstructor
public class PythonGateController {

    private final PythonGateService pythonGateService;

    // 차단기 카메라가 인식한 번호판이 등록 차량인지 확인한다.
    @PostMapping({"/api/gate/check", "/api/check-plate"})
    public ResponseEntity<Map<String, Object>> checkPlate(@RequestBody PythonGateCheckRequestDto requestDto) {
        String plate = requestDto != null ? requestDto.getPlate() : null;
        Integer apartmentNo = requestDto != null ? requestDto.getApartmentNo() : null;
        return ResponseEntity.ok(pythonGateService.checkPlate(plate, apartmentNo));
    }

    // Python/아두이노 장비가 차단기 상시개방 여부를 주기적으로 확인할 때 사용한다.
    @GetMapping("/api/gate/control")
    public ResponseEntity<Map<String, Object>> findGateControl(@RequestParam(required = false) Integer apartmentNo) {
        return ResponseEntity.ok(pythonGateService.findGateControl(apartmentNo));
    }

    // 차단기 통과 결과를 gate_entry_log 테이블에 저장한다.
    @PostMapping({"/api/gate/log", "/api/entry-log"})
    public ResponseEntity<Map<String, Object>> saveGateLog(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(pythonGateService.saveGateLog(request));
    }

    // 번호판을 아직 모르는 주차 기록을 Python 서버가 조회할 때 사용한다.
    @GetMapping("/api/gate/unmatched")
    public ResponseEntity<Object> findUnmatchedHistories() {
        return ResponseEntity.ok(pythonGateService.findUnmatchedHistories());
    }

    // 차단기에서 인식한 번호판을 기존 UNKNOWN 주차 기록에 연결한다.
    @PostMapping("/api/gate/assign-plate")
    public ResponseEntity<Map<String, Object>> assignPlate(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(pythonGateService.assignPlate(request));
    }

    // 이중주차 알림 요청을 받는 임시 API이다. 저장 테이블이 생기면 저장 로직을 연결한다.
    @PostMapping("/api/gate/alert")
    public ResponseEntity<Map<String, Object>> saveDoubleParkingAlert(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(pythonGateService.saveDoubleParkingAlert(request));
    }
}
