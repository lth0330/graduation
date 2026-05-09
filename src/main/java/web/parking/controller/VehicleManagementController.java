package web.parking.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import web.parking.dto.VehicleManagementDto;
import web.parking.dto.VehicleSaveRequestDto;
import web.parking.service.VehicleManagementService;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleManagementController {

    private final VehicleManagementService vehicleManagementService;

    @GetMapping
    public ResponseEntity<List<VehicleManagementDto>> findVehicles(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(vehicleManagementService.findVehicles(apartmentNo));
    }

    @GetMapping("/{vehicleNo}")
    public ResponseEntity<VehicleManagementDto> findVehicle(@PathVariable Integer vehicleNo) {
        return ResponseEntity.ok(vehicleManagementService.findVehicle(vehicleNo));
    }

    @PostMapping
    public ResponseEntity<VehicleManagementDto> create(@RequestBody VehicleSaveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleManagementService.create(requestDto));
    }

    @PutMapping("/{vehicleNo}")
    public ResponseEntity<VehicleManagementDto> update(
            @PathVariable Integer vehicleNo,
            @RequestBody VehicleSaveRequestDto requestDto
    ) {
        return ResponseEntity.ok(vehicleManagementService.update(vehicleNo, requestDto));
    }

    @DeleteMapping("/{vehicleNo}")
    public ResponseEntity<Void> delete(@PathVariable Integer vehicleNo) {
        vehicleManagementService.delete(vehicleNo);
        return ResponseEntity.noContent().build();
    }
}
