package app.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import app.dto.AppFindIdRequestDto;
import app.dto.AppLoginRequestDto;
import app.dto.AppResetPasswordRequestDto;
import app.dto.AppSignupRequestDto;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.common.auth.JwtProvider;
import web.common.type.ApprovalStatus;
import web.common.type.UserRole;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 앱 인증 서비스: 입주민 로그인/회원가입/계정 찾기/비밀번호 변경과 JWT 발급을 담당한다.
public class AppAuthService {

    private final ResidentRepository residentRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public Map<String, Object> login(AppLoginRequestDto requestDto) {
        if (isBlank(requestDto.getLoginId()) || isBlank(requestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "loginId and password are required.");
        }

        // 앱 로그인은 기존 입주민 테이블(user)을 그대로 사용한다.
        ResidentEntity resident = residentRepository.findByLoginId(requestDto.getLoginId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));

        if (!matchesPassword(requestDto.getPassword(), resident.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password does not match.");
        }

        if (resident.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resident signup is not approved.");
        }

        Integer apartmentNo = resident.getApartment() != null ? resident.getApartment().getNo() : null;
        // 발급된 토큰에는 입주민 번호, 권한, 아파트 번호가 들어가 이후 API 권한 확인에 사용된다.
        String token = jwtProvider.generateToken(resident.getNo(), UserRole.RESIDENT, apartmentNo, resident.getLoginId());

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("u_id", resident.getLoginId());
        user.put("approval_status", resident.getApprovalStatus().name());

        Map<String, Object> response = success();
        response.put("token", token);
        response.put("user", user);
        return response;
    }

    @Transactional
    public Map<String, Object> signup(AppSignupRequestDto requestDto) {
        validateSignup(requestDto);

        if (residentRepository.existsByLoginId(requestDto.getLoginId().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login ID already exists.");
        }

        ApartmentEntity apartment = apartmentRepository.findById(requestDto.getApartmentNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apartment not found."));

        // 아파트 비밀번호가 맞아야 해당 아파트 입주민으로 가입할 수 있다.
        if (!apartment.getPassword().equals(requestDto.getApartmentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apartment password does not match.");
        }

        ResidentEntity resident = ResidentEntity.builder()
                .apartment(apartment)
                .loginId(requestDto.getLoginId().trim())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName().trim())
                .email(requestDto.getEmail().trim())
                .phone(trimToNull(requestDto.getPhone()))
                .dong(requestDto.getDong().trim())
                .ho(requestDto.getHo().trim())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        residentRepository.save(resident);
        return success();
    }

    public Map<String, Object> findId(AppFindIdRequestDto requestDto) {
        if (isBlank(requestDto.getDong()) || isBlank(requestDto.getHo()) || isBlank(requestDto.getApartmentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dong, ho and apartmentPassword are required.");
        }

        // 동/호수와 아파트 비밀번호로 입주민 계정을 찾는다.
        List<ResidentEntity> residents = residentRepository.findByDongAndHoAndApartment_Password(
                requestDto.getDong().trim(),
                requestDto.getHo().trim(),
                requestDto.getApartmentPassword().trim()
        );

        ResidentEntity resident = residents.stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));

        Map<String, Object> response = success();
        response.put("u_id", resident.getLoginId());
        return response;
    }

    @Transactional
    public Map<String, Object> resetPassword(AppResetPasswordRequestDto requestDto) {
        if (isBlank(requestDto.getLoginId())
                || isBlank(requestDto.getDong())
                || isBlank(requestDto.getHo())
                || isBlank(requestDto.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "loginId, dong, ho and newPassword are required.");
        }

        // 비밀번호 재설정은 아이디와 세대 정보가 모두 일치할 때만 허용한다.
        ResidentEntity resident = residentRepository.findByLoginId(requestDto.getLoginId().trim())
                .filter(candidate -> candidate.getDong().equals(requestDto.getDong().trim()))
                .filter(candidate -> candidate.getHo().equals(requestDto.getHo().trim()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));

        resident.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        return success();
    }

    public Map<String, Object> userInfo(Integer residentNo) {
        ResidentEntity resident = residentRepository.findById(residentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found."));

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("u_name", resident.getName());
        user.put("u_dong", resident.getDong());
        user.put("u_ho", resident.getHo());
        user.put("a_name", resident.getApartment() != null ? resident.getApartment().getName() : ""); 
        // 💡 [추가] 앱에서 차량 등록 팝업 띄울 때 사용할 진짜 제한 대수 정보 넘겨주기!
        user.put("resident_car_limit", resident.getResidentCarLimit());
        user.put("visitor_car_limit", resident.getVisitorCarLimit());
        Map<String, Object> response = success();
        response.put("user", user);
        return response;
    }

    private boolean matchesPassword(String rawPassword, String savedPassword) {
        // BCrypt로 저장된 비밀번호와 기존 평문 샘플 비밀번호를 모두 처리한다.
        if (savedPassword != null && savedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, savedPassword);
        }
        return rawPassword.equals(savedPassword);
    }

    private void validateSignup(AppSignupRequestDto requestDto) {
        if (requestDto.getApartmentNo() == null
                || isBlank(requestDto.getApartmentPassword())
                || isBlank(requestDto.getLoginId())
                || isBlank(requestDto.getPassword())
                || isBlank(requestDto.getName())
                || isBlank(requestDto.getEmail())
                || isBlank(requestDto.getDong())
                || isBlank(requestDto.getHo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required signup fields are missing.");
        }
    }

    private Map<String, Object> success() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
