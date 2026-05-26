package python.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import python.dto.PythonGateCheckRequestDto;
import python.service.PythonGateService;

@RestController
@RequiredArgsConstructor
public class PythonGateController {

    private final PythonGateService pythonGateService;

    @PostMapping("/api/gate/check")
    public ResponseEntity<Map<String, Object>> checkPlate(@RequestBody PythonGateCheckRequestDto requestDto) {
        String plate = requestDto != null ? requestDto.getPlate() : null;
        return ResponseEntity.ok(pythonGateService.checkPlate(plate));
    }
}
