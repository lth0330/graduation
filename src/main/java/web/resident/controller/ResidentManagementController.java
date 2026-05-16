package web.resident.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.resident.dto.ResidentCreateRequestDto;
import web.resident.dto.ResidentManagementDto;
import web.resident.dto.ResidentUpdateRequestDto;
import web.resident.service.ResidentManagementService;

@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentManagementController {

    private final ResidentManagementService residentManagementService;

    @GetMapping
    public ResponseEntity<List<ResidentManagementDto>> findApprovedResidents(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(residentManagementService.findApprovedResidents(apartmentNo));
    }

    @GetMapping("/{residentNo}")
    public ResponseEntity<ResidentManagementDto> findResident(@PathVariable Integer residentNo) {
        return ResponseEntity.ok(residentManagementService.findResident(residentNo));
    }

    @PostMapping
    public ResponseEntity<ResidentManagementDto> create(@RequestBody ResidentCreateRequestDto requestDto) {
        return ResponseEntity.ok(residentManagementService.create(requestDto));
    }

    @PutMapping("/{residentNo}")
    public ResponseEntity<ResidentManagementDto> update(
            @PathVariable Integer residentNo,
            @RequestBody ResidentUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(residentManagementService.update(residentNo, requestDto));
    }

    @DeleteMapping("/{residentNo}")
    public ResponseEntity<Void> delete(@PathVariable Integer residentNo) {
        residentManagementService.delete(residentNo);
        return ResponseEntity.noContent().build();
    }
}
