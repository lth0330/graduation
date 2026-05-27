package python.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import python.dto.PythonGateCheckRequestDto;
import python.service.PythonGateService;

@RestController
@RequiredArgsConstructor
public class PythonGateController {

    private final PythonGateService pythonGateService;

    @PostMapping({"/api/gate/check", "/api/check-plate"})
    public ResponseEntity<Map<String, Object>> checkPlate(@RequestBody PythonGateCheckRequestDto requestDto) {
        String plate = requestDto != null ? requestDto.getPlate() : null;
        return ResponseEntity.ok(pythonGateService.checkPlate(plate));
    }

    @PostMapping({"/api/gate/log", "/api/entry-log"})
    public ResponseEntity<Map<String, Object>> saveGateLog(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(pythonGateService.saveGateLog(request));
    }

    @GetMapping("/api/gate/unmatched")
    public ResponseEntity<Object> findUnmatchedHistories() {
        return ResponseEntity.ok(pythonGateService.findUnmatchedHistories());
    }

    @PostMapping("/api/gate/assign-plate")
    public ResponseEntity<Map<String, Object>> assignPlate(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(pythonGateService.assignPlate(request));
    }

    @PostMapping("/api/gate/alert")
    public ResponseEntity<Map<String, Object>> saveDoubleParkingAlert(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(pythonGateService.saveDoubleParkingAlert(request));
    }
}
