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
// 웹 주차장 관리 컨트롤러: 주차장 층/구역 묶음의 조회, 등록, 삭제를 담당한다.
public class ParkingLotManagementController {

    private final ParkingLotManagementService parkingLotManagementService;

    @GetMapping
    // Read: 특정 아파트의 주차장 목록을 조회한다.
    public ResponseEntity<List<ParkingLotDto>> findParkingLots(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(parkingLotManagementService.findParkingLots(apartmentNo));
    }

    @PostMapping
    // Create: 주차장 정보를 새로 등록한다.
    public ResponseEntity<ParkingLotDto> create(@RequestBody ParkingLotSaveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingLotManagementService.create(requestDto));
    }

    @DeleteMapping("/{parkingLotNo}")
    // Delete: 주차장 정보를 삭제한다.
    public ResponseEntity<Void> delete(@PathVariable Integer parkingLotNo) {
        parkingLotManagementService.delete(parkingLotNo);
        return ResponseEntity.noContent().build();
    }
}
