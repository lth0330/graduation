package web.parking.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import web.parking.dto.ParkingHistoryDto;
import web.parking.service.ParkingHistoryQueryService;

@RestController
@RequiredArgsConstructor
public class ParkingHistoryQueryController {

    private final ParkingHistoryQueryService parkingHistoryQueryService;

    @GetMapping("/api/parking-histories/{historyId}")
    public ResponseEntity<ParkingHistoryDto> findMyParkingHistory(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer historyId
    ) {
        return ResponseEntity.ok(parkingHistoryQueryService.findMyParkingHistory(principal, historyId));
    }
}
