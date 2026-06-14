package web.parking.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import web.parking.dto.VehicleOwnerDto;
import web.parking.dto.VehicleSaveRequestDto;
import web.parking.service.VehicleManagementService;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
// 웹 차량 관리 컨트롤러: 아파트 관리자 화면의 입주민 차량 CRUD를 담당한다.
public class VehicleManagementController {

    private final VehicleManagementService vehicleManagementService;

    @GetMapping
    // Read: 특정 아파트의 차량 목록을 조회한다.
    public ResponseEntity<List<VehicleManagementDto>> findVehicles(@RequestParam Integer apartmentNo) {
        return ResponseEntity.ok(vehicleManagementService.findVehicles(apartmentNo));
    }

    @GetMapping("/owner")
    public ResponseEntity<VehicleOwnerDto> findOwnerByCarNumber(
            @AuthenticationPrincipal Map<String, Object> principal,
            @RequestParam String carNumber
    ) {
        return ResponseEntity.ok(vehicleManagementService.findOwnerByCarNumber(principal, carNumber));
    }

    @GetMapping("/{vehicleNo}")
    // Read: 차량 번호(PK)로 차량 상세를 조회한다.
    public ResponseEntity<VehicleManagementDto> findVehicle(@PathVariable Integer vehicleNo) {
        return ResponseEntity.ok(vehicleManagementService.findVehicle(vehicleNo));
    }

    @PostMapping
    // Create: 입주민 차량을 새로 등록한다.
    public ResponseEntity<VehicleManagementDto> create(@RequestBody VehicleSaveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleManagementService.create(requestDto));
    }

    @PutMapping("/{vehicleNo}")
    // Update: 차량번호, 종류, 비고 등을 수정한다.
    public ResponseEntity<VehicleManagementDto> update(
            @PathVariable Integer vehicleNo,
            @RequestBody VehicleSaveRequestDto requestDto
    ) {
        return ResponseEntity.ok(vehicleManagementService.update(vehicleNo, requestDto));
    }

    @DeleteMapping("/{vehicleNo}")
    // Delete: 차량 정보를 삭제한다.
    public ResponseEntity<Void> delete(@PathVariable Integer vehicleNo) {
        vehicleManagementService.delete(vehicleNo);
        return ResponseEntity.noContent().build();
    }
}
