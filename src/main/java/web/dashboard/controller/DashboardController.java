package web.dashboard.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import web.dashboard.dto.ApartmentManagerDashboardSummaryDto;
import web.dashboard.dto.WebAdminDashboardSummaryDto;
import web.dashboard.service.DashboardService;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/web-admin/dashboard/summary")
    public ResponseEntity<WebAdminDashboardSummaryDto> getWebAdminSummary() {
        return ResponseEntity.ok(dashboardService.getWebAdminSummary());
    }

    @GetMapping("/api/apartment-managers/dashboard/summary")
    public ResponseEntity<ApartmentManagerDashboardSummaryDto> getApartmentManagerSummary(
            @AuthenticationPrincipal Map<String, Object> principal
    ) {
        return ResponseEntity.ok(dashboardService.getApartmentManagerSummary(principal));
    }
}
