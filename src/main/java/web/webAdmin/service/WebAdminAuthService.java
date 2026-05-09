package web.webAdmin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.webAdmin.dto.WebAdminLoginRequestDto;
import web.webAdmin.dto.WebAdminLoginResponseDto;
import web.webAdmin.entity.WebManagerEntity;
import web.webAdmin.repository.WebManagerRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebAdminAuthService {

    private final WebManagerRepository webManagerRepository;
    private final PasswordEncoder passwordEncoder;

    public WebAdminLoginResponseDto login(WebAdminLoginRequestDto requestDto) {
        validateLoginRequest(requestDto);

        WebManagerEntity webManager = webManagerRepository.findByWId(requestDto.getWId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getWPwd(), webManager.getWPwd())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return WebAdminLoginResponseDto.builder()
                .managerNo(webManager.getNo())
                .wId(webManager.getWId())
                .build();
    }

    private void validateLoginRequest(WebAdminLoginRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getWId()) || isBlank(requestDto.getWPwd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
