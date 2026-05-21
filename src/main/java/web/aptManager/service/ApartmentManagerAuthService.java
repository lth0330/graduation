package web.aptManager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.dto.ApartmentManagerLoginRequestDto;
import web.aptManager.dto.ApartmentManagerLoginResponseDto;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.common.auth.JwtProvider;
import web.common.type.ApprovalStatus;
import web.common.type.UserRole;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 아파트 관리자 인증 서비스: 승인 상태와 비밀번호를 확인해 APARTMENT_MANAGER 토큰을 발급한다.
public class ApartmentManagerAuthService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public ApartmentManagerLoginResponseDto login(ApartmentManagerLoginRequestDto requestDto) {
        // 인증: 승인된 관리자만 로그인할 수 있게 검증한다.
        validateLoginRequest(requestDto);

        ApartmentManagerEntity manager = apartmentManagerRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "loginId or password is invalid."));

        if (!passwordEncoder.matches(requestDto.getPassword(), manager.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "loginId or password is invalid.");
        }

        if (manager.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only approved apartment managers can login.");
        }

        ApartmentEntity apartment = manager.getApartment();
        Integer apartmentNo = apartment != null ? apartment.getNo() : null;
        String accessToken = jwtProvider.generateToken(
                manager.getNo(),
                UserRole.APARTMENT_MANAGER,
                apartmentNo,
                manager.getLoginId()
        );

        return ApartmentManagerLoginResponseDto.builder()
                .managerNo(manager.getNo())
                .apartmentNo(apartmentNo)
                .apartmentName(apartment != null ? apartment.getName() : null)
                .loginId(manager.getLoginId())
                .name(manager.getName())
                .approvalStatus(manager.getApprovalStatus())
                .tokenType("Bearer")
                .accessToken(accessToken)
                .build();
    }

    private void validateLoginRequest(ApartmentManagerLoginRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getLoginId()) || isBlank(requestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "loginId and password are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
