package web.parking.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.parking.dto.ParkingLotDto;
import web.parking.dto.ParkingLotSaveRequestDto;
import web.parking.service.ParkingLotManagementService;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotManagementController {

    private final ParkingLotManagementService parkingLotManagementService;

    @GetMapping
    public ResponseEntity<List<ParkingLotDto>> findParkingLots(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(parkingLotManagementService.findParkingLots(apartmentNo));
    }

    @PostMapping
    public ResponseEntity<ParkingLotDto> create(@RequestBody ParkingLotSaveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingLotManagementService.create(requestDto));
    }

    @DeleteMapping("/{parkingLotNo}")
    public ResponseEntity<Void> delete(@PathVariable Integer parkingLotNo) {
        parkingLotManagementService.delete(parkingLotNo);
        return ResponseEntity.noContent().build();
    }
}
