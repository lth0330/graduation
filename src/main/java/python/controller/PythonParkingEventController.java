package python.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import python.dto.PythonParkingEntryRequestDto;
import python.dto.PythonParkingExitRequestDto;
import python.dto.PythonParkingPlateUpdateRequestDto;
import python.service.PythonParkingEventService;

@RestController
@RequiredArgsConstructor
public class PythonParkingEventController {

    private final PythonParkingEventService pythonParkingEventService;

    // Python 번호판 보정용으로 DB에 등록된 차량번호 목록을 내려준다.
    @GetMapping("/api/parking/cars")
    public ResponseEntity<List<Map<String, String>>> findCarNumbers() {
        return ResponseEntity.ok(pythonParkingEventService.findCarNumbers());
    }

    // 특정 주차칸의 현재 상태와 차량번호를 조회한다.
    @GetMapping("/api/parking/zone/{zoneName}")
    public ResponseEntity<Map<String, Object>> findZoneStatus(@PathVariable String zoneName) {
        return ResponseEntity.ok(pythonParkingEventService.findZoneStatus(zoneName));
    }

    // 전체 주차장 점유율을 조회한다. FastAPI 차단기 제어에서 만차 여부 판단에 사용한다.
    @GetMapping("/api/parking/occupancy")
    public ResponseEntity<Map<String, Object>> findOccupancy() {
        return ResponseEntity.ok(pythonParkingEventService.findOccupancy());
    }

    // Python이 입차 이벤트를 보내면 주차칸 상태와 주차 이력을 저장한다.
    @PostMapping("/api/parking/entry")
    public ResponseEntity<Map<String, Object>> saveEntry(@RequestBody PythonParkingEntryRequestDto requestDto) {
        return ResponseEntity.ok(pythonParkingEventService.saveEntry(requestDto));
    }

    // Python이 출차 이벤트를 보내면 주차 이력을 종료하고 주차칸을 비운다.
    @PostMapping("/api/parking/exit")
    public ResponseEntity<Map<String, Object>> saveExit(@RequestBody PythonParkingExitRequestDto requestDto) {
        return ResponseEntity.ok(pythonParkingEventService.saveExit(requestDto));
    }

    // 입차 후 번호판이 새로 확인되면 기존 주차 기록의 번호판을 갱신한다.
    @PostMapping("/api/parking/update-plate")
    public ResponseEntity<Map<String, Object>> updatePlate(@RequestBody PythonParkingPlateUpdateRequestDto requestDto) {
        return ResponseEntity.ok(pythonParkingEventService.updatePlate(requestDto));
    }
}
