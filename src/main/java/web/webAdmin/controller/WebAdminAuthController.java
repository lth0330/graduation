package web.webAdmin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.webAdmin.dto.WebAdminLoginRequestDto;
import web.webAdmin.dto.WebAdminLoginResponseDto;
import web.webAdmin.service.WebAdminAuthService;

@RestController
@RequestMapping("/api/web-admin")
@RequiredArgsConstructor
public class WebAdminAuthController {

    private final WebAdminAuthService webAdminAuthService;

    @PostMapping("/login")
    public ResponseEntity<WebAdminLoginResponseDto> login(@RequestBody WebAdminLoginRequestDto requestDto) {
        return ResponseEntity.ok(webAdminAuthService.login(requestDto));
    }
}
