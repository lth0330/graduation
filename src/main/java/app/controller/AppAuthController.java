package app.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import app.dto.AppFindIdRequestDto;
import app.dto.AppLoginRequestDto;
import app.dto.AppResetPasswordRequestDto;
import app.dto.AppSignupRequestDto;
import app.service.AppAuthService;

@RestController
@RequiredArgsConstructor
// 앱 입주민 인증 컨트롤러: 로그인, 회원가입, 아이디 찾기, 비밀번호 재설정, 내 정보 조회를 담당한다.
public class AppAuthController {

    private final AppAuthService appAuthService;

    @PostMapping("/api/login")
    // 인증: 입주민 로그인 후 JWT 토큰을 발급한다.
    public ResponseEntity<Map<String, Object>> login(@RequestBody AppLoginRequestDto requestDto) {
        return ResponseEntity.ok(appAuthService.login(requestDto));
    }

    @PostMapping("/api/signup")
    // Create: 앱에서 입주민 계정을 새로 등록한다.
    public ResponseEntity<Map<String, Object>> signup(@RequestBody AppSignupRequestDto requestDto) {
        return ResponseEntity.ok(appAuthService.signup(requestDto));
    }

    @PostMapping("/api/find-id")
    // Read: 동/호수 정보로 가입된 아이디를 조회한다.
    public ResponseEntity<Map<String, Object>> findId(@RequestBody AppFindIdRequestDto requestDto) {
        return ResponseEntity.ok(appAuthService.findId(requestDto));
    }

    @PostMapping("/api/reset-pw")
    // Update: 본인 확인 후 입주민 비밀번호를 변경한다.
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody AppResetPasswordRequestDto requestDto) {
        return ResponseEntity.ok(appAuthService.resetPassword(requestDto));
    }

    @GetMapping("/api/user-info")
    // Read: JWT 토큰의 입주민 번호로 내 정보를 조회한다.
    public ResponseEntity<Map<String, Object>> userInfo(Authentication authentication) {
        return ResponseEntity.ok(appAuthService.userInfo(getUserNo(authentication)));
    }

    private Integer getUserNo(Authentication authentication) {
        Map<?, ?> principal = (Map<?, ?>) authentication.getPrincipal();
        Object userNo = principal.get("userNo");
        return userNo instanceof Integer integerUserNo ? integerUserNo : Integer.valueOf(userNo.toString());
    }
}
