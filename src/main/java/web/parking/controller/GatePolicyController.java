package web.parking.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import web.parking.dto.GatePolicyDto;
import web.parking.dto.GatePolicyRequestDto;
import web.parking.service.GatePolicyService;

@RestController
@RequiredArgsConstructor
public class GatePolicyController {

    private final GatePolicyService gatePolicyService;

    @GetMapping("/api/apartment-managers/gate-policy")
    public ResponseEntity<GatePolicyDto> findMyPolicy(@AuthenticationPrincipal Map<String, Object> principal) {
        return ResponseEntity.ok(gatePolicyService.findMyPolicy(principal));
    }

    @PatchMapping("/api/apartment-managers/gate-policy")
    public ResponseEntity<GatePolicyDto> updateMyPolicy(
            @AuthenticationPrincipal Map<String, Object> principal,
            @RequestBody GatePolicyRequestDto requestDto
    ) {
        return ResponseEntity.ok(gatePolicyService.updateMyPolicy(principal, requestDto));
    }
}
