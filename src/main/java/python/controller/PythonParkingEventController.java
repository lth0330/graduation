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

    @GetMapping("/api/parking/cars")
    public ResponseEntity<List<Map<String, String>>> findCarNumbers() {
        return ResponseEntity.ok(pythonParkingEventService.findCarNumbers());
    }

    @GetMapping("/api/parking/zone/{zoneName}")
    public ResponseEntity<Map<String, Object>> findZoneStatus(@PathVariable String zoneName) {
        return ResponseEntity.ok(pythonParkingEventService.findZoneStatus(zoneName));
    }

    @PostMapping("/api/parking/entry")
    public ResponseEntity<Map<String, Object>> saveEntry(@RequestBody PythonParkingEntryRequestDto requestDto) {
        return ResponseEntity.ok(pythonParkingEventService.saveEntry(requestDto));
    }

    @PostMapping("/api/parking/exit")
    public ResponseEntity<Map<String, Object>> saveExit(@RequestBody PythonParkingExitRequestDto requestDto) {
        return ResponseEntity.ok(pythonParkingEventService.saveExit(requestDto));
    }

    @PostMapping("/api/parking/update-plate")
    public ResponseEntity<Map<String, Object>> updatePlate(@RequestBody PythonParkingPlateUpdateRequestDto requestDto) {
        return ResponseEntity.ok(pythonParkingEventService.updatePlate(requestDto));
    }
}
