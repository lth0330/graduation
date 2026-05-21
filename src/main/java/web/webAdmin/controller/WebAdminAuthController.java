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
// 웹 최고 관리자 인증 컨트롤러: 웹 관리자 로그인을 담당한다.
public class WebAdminAuthController {

    private final WebAdminAuthService webAdminAuthService;

    @PostMapping("/login")
    // 인증: 웹 관리자 로그인 후 JWT 토큰을 발급한다.
    public ResponseEntity<WebAdminLoginResponseDto> login(@RequestBody WebAdminLoginRequestDto requestDto) {
        WebAdminLoginResponseDto responseDto = webAdminAuthService.login(requestDto);
        return ResponseEntity.ok()
                .header("Authorization", responseDto.getTokenType() + " " + responseDto.getAccessToken())
                .body(responseDto);
    }
}
