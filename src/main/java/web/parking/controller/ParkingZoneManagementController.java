package web.parking.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.parking.dto.ParkingZoneDto;
import web.parking.dto.ParkingZoneLayoutRequestDto;
import web.parking.dto.ParkingZoneSaveRequestDto;
import web.parking.dto.ParkingZoneStatusRequestDto;
import web.parking.service.ParkingZoneManagementService;

@RestController
@RequestMapping("/api/parking-zones")
@RequiredArgsConstructor
public class ParkingZoneManagementController {

    private final ParkingZoneManagementService parkingZoneManagementService;

    @GetMapping
    public ResponseEntity<List<ParkingZoneDto>> findParkingZones(@RequestParam Integer parkingLotNo) {
        return ResponseEntity.ok(parkingZoneManagementService.findParkingZones(parkingLotNo));
    }

    @PostMapping
    public ResponseEntity<ParkingZoneDto> create(@RequestBody ParkingZoneSaveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingZoneManagementService.create(requestDto));
    }

    @PatchMapping("/{parkingZoneNo}/status")
    public ResponseEntity<ParkingZoneDto> updateStatus(
            @PathVariable Integer parkingZoneNo,
            @RequestBody ParkingZoneStatusRequestDto requestDto
    ) {
        return ResponseEntity.ok(parkingZoneManagementService.updateStatus(parkingZoneNo, requestDto));
    }

    @PatchMapping("/{parkingZoneNo}/layout")
    public ResponseEntity<ParkingZoneDto> updateLayout(
            @PathVariable Integer parkingZoneNo,
            @RequestBody ParkingZoneLayoutRequestDto requestDto
    ) {
        return ResponseEntity.ok(parkingZoneManagementService.updateLayout(parkingZoneNo, requestDto));
    }

    @DeleteMapping("/{parkingZoneNo}")
    public ResponseEntity<Void> delete(@PathVariable Integer parkingZoneNo) {
        parkingZoneManagementService.delete(parkingZoneNo);
        return ResponseEntity.noContent().build();
    }
}
