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
public class ApartmentManagerAuthController {

    private final ApartmentManagerAuthService apartmentManagerAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApartmentManagerLoginResponseDto> login(@RequestBody ApartmentManagerLoginRequestDto requestDto) {
        return ResponseEntity.ok(apartmentManagerAuthService.login(requestDto));
    }
}
