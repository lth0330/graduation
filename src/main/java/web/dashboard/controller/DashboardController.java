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
// 웹 대시보드 컨트롤러: 웹 관리자와 아파트 관리자의 요약 통계를 조회한다.
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/web-admin/dashboard/summary")
    // Read: 전체 서비스 기준의 웹 관리자 대시보드 통계를 조회한다.
    public ResponseEntity<WebAdminDashboardSummaryDto> getWebAdminSummary() {
        return ResponseEntity.ok(dashboardService.getWebAdminSummary());
    }

    @GetMapping("/api/apartment-managers/dashboard/summary")
    // Read: 로그인한 아파트 관리자의 아파트 기준 통계를 조회한다.
    public ResponseEntity<ApartmentManagerDashboardSummaryDto> getApartmentManagerSummary(
            @AuthenticationPrincipal Map<String, Object> principal
    ) {
        return ResponseEntity.ok(dashboardService.getApartmentManagerSummary(principal));
    }
}
