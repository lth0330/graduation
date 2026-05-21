package web.parking.controller;

import java.util.List;
import java.util.Map;
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
// 웹 주차구역 관리 컨트롤러: 개별 주차 슬롯의 조회, 등록, 상태/배치 수정, 삭제를 담당한다.
public class ParkingZoneManagementController {

    private final ParkingZoneManagementService parkingZoneManagementService;

    @GetMapping
    // Read: 특정 주차장에 속한 주차구역 목록을 조회한다.
    public ResponseEntity<List<ParkingZoneDto>> findParkingZones(@RequestParam Integer parkingLotNo) {
        return ResponseEntity.ok(parkingZoneManagementService.findParkingZones(parkingLotNo));
    }

    @PostMapping
    // Create: 주차구역을 새로 등록한다.
    public ResponseEntity<ParkingZoneDto> create(@RequestBody ParkingZoneSaveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingZoneManagementService.create(requestDto));
    }

    @PatchMapping("/{parkingZoneNo}/status")
    // Update: 주차구역의 사용 상태를 변경한다.
    public ResponseEntity<ParkingZoneDto> updateStatus(
            @PathVariable Integer parkingZoneNo,
            @RequestBody ParkingZoneStatusRequestDto requestDto
    ) {
        return ResponseEntity.ok(parkingZoneManagementService.updateStatus(parkingZoneNo, requestDto));
    }

    @PatchMapping("/{parkingZoneNo}/layout")
    // Update: 주차구역의 위치/크기 같은 화면 배치 정보를 수정한다.
    public ResponseEntity<ParkingZoneDto> updateLayout(
            @PathVariable Integer parkingZoneNo,
            @RequestBody ParkingZoneLayoutRequestDto requestDto
    ) {
        return ResponseEntity.ok(parkingZoneManagementService.updateLayout(parkingZoneNo, requestDto));
    }

    @DeleteMapping("/{parkingZoneNo}")
    // Delete: 주차구역을 삭제한다.
    public ResponseEntity<Void> delete(@PathVariable Integer parkingZoneNo) {
        parkingZoneManagementService.delete(parkingZoneNo);
        return ResponseEntity.noContent().build();
    }
}
