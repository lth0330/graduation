package web.aptManager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.aptManager.dto.ApartmentManagerLoginRequestDto;
import web.aptManager.dto.ApartmentManagerLoginResponseDto;
import web.aptManager.service.ApartmentManagerAuthService;

@RestController
@RequestMapping("/api/apartment-managers")
@RequiredArgsConstructor
// 아파트 관리자 인증 컨트롤러: 관리자 로그인을 담당한다.
public class ApartmentManagerAuthController {

    private final ApartmentManagerAuthService apartmentManagerAuthService;

    @PostMapping("/login")
    // 인증: 승인된 아파트 관리자 계정으로 로그인하고 JWT를 발급한다.
    public ResponseEntity<ApartmentManagerLoginResponseDto> login(@RequestBody ApartmentManagerLoginRequestDto requestDto) {
        ApartmentManagerLoginResponseDto responseDto = apartmentManagerAuthService.login(requestDto);
        return ResponseEntity.ok()
                .header("Authorization", responseDto.getTokenType() + " " + responseDto.getAccessToken())
                .body(responseDto);
    }
}
