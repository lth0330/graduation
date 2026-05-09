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
import web.common.type.ApprovalStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApartmentManagerAuthService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final PasswordEncoder passwordEncoder;

    public ApartmentManagerLoginResponseDto login(ApartmentManagerLoginRequestDto requestDto) {
        validateLoginRequest(requestDto);

        ApartmentManagerEntity manager = apartmentManagerRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), manager.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (manager.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "승인된 아파트 관리자만 로그인할 수 있습니다.");
        }

        ApartmentEntity apartment = manager.getApartment();

        return ApartmentManagerLoginResponseDto.builder()
                .managerNo(manager.getNo())
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .apartmentName(apartment != null ? apartment.getName() : null)
                .loginId(manager.getLoginId())
                .name(manager.getName())
                .approvalStatus(manager.getApprovalStatus())
                .build();
    }

    private void validateLoginRequest(ApartmentManagerLoginRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getLoginId()) || isBlank(requestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
