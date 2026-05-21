package web.webAdmin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.common.auth.JwtProvider;
import web.common.type.UserRole;
import web.webAdmin.dto.WebAdminLoginRequestDto;
import web.webAdmin.dto.WebAdminLoginResponseDto;
import web.webAdmin.entity.WebManagerEntity;
import web.webAdmin.repository.WebManagerRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 웹 최고 관리자 인증 서비스: 계정 검증 후 WEB_ADMIN 권한 토큰을 만든다.
public class WebAdminAuthService {

    private final WebManagerRepository webManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public WebAdminLoginResponseDto login(WebAdminLoginRequestDto requestDto) {
        // 인증: 아이디/비밀번호를 확인하고 웹 관리자 JWT를 발급한다.
        validateLoginRequest(requestDto);

        WebManagerEntity webManager = webManagerRepository.findByWId(requestDto.getWId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "wId or password is invalid."));

        if (!passwordEncoder.matches(requestDto.getWPwd(), webManager.getWPwd())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "wId or password is invalid.");
        }

        String accessToken = jwtProvider.generateToken(
                webManager.getNo(),
                UserRole.WEB_ADMIN,
                null,
                webManager.getWId()
        );

        return WebAdminLoginResponseDto.builder()
                .managerNo(webManager.getNo())
                .wId(webManager.getWId())
                .tokenType("Bearer")
                .accessToken(accessToken)
                .build();
    }

    private void validateLoginRequest(WebAdminLoginRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getWId()) || isBlank(requestDto.getWPwd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wId and password are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
