package web.aptManager.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.dto.ApartmentManagerSignupRequestDto;
import web.aptManager.dto.SignDto;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.aptManager.repository.ApartmentRepository;
import web.common.type.ApprovalStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignDto signup(ApartmentManagerSignupRequestDto requestDto) {
        validateSignupRequest(requestDto);
        validateDuplicate(requestDto);

        ApartmentEntity apartment = findOrCreateApartment(requestDto);

        ApartmentManagerEntity manager = ApartmentManagerEntity.builder()
                .apartment(apartment)
                .loginId(requestDto.getLoginId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .address(joinAddress(requestDto.getAddress(), requestDto.getDetailAddress()))
                .name(requestDto.getName())
                .picture(requestDto.getCareerImage())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        ApartmentManagerEntity savedEntity = apartmentManagerRepository.save(manager);
        return savedEntity.toDTO();
    }

    public List<SignDto> findAll() {
        return apartmentManagerRepository.findAll()
                .stream()
                .map(ApartmentManagerEntity::toDTO)
                .toList();
    }

    public SignDto findByNo(Integer managerNo) {
        return findEntity(managerNo).toDTO();
    }

    public List<SignDto> findByApartmentNo(Integer apartmentNo) {
        return apartmentManagerRepository.findByApartment_No(apartmentNo)
                .stream()
                .map(ApartmentManagerEntity::toDTO)
                .toList();
    }

    @Transactional
    public SignDto update(Integer managerNo, SignDto signDto) {
        ApartmentManagerEntity entity = findEntity(managerNo);

        if (signDto.getApartmentNo() != null) {
            ApartmentEntity apartment = apartmentRepository.findById(signDto.getApartmentNo())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트입니다."));
            entity.setApartment(apartment);
        }

        if (signDto.getLoginId() != null && !signDto.getLoginId().equals(entity.getLoginId())) {
            if (apartmentManagerRepository.existsByLoginId(signDto.getLoginId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
            }
            entity.setLoginId(signDto.getLoginId());
        }

        if (signDto.getEmail() != null && !signDto.getEmail().equals(entity.getEmail())) {
            if (apartmentManagerRepository.existsByEmail(signDto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
            }
            entity.setEmail(signDto.getEmail());
        }

        if (signDto.getPassword() != null) {
            entity.setPassword(passwordEncoder.encode(signDto.getPassword()));
        }
        if (signDto.getPhone() != null) {
            entity.setPhone(signDto.getPhone());
        }
        if (signDto.getAddress() != null) {
            entity.setAddress(signDto.getAddress());
        }
        if (signDto.getName() != null) {
            entity.setName(signDto.getName());
        }
        if (signDto.getPicture() != null) {
            entity.setPicture(signDto.getPicture());
        }

        return entity.toDTO();
    }

    @Transactional
    public void delete(Integer managerNo) {
        ApartmentManagerEntity entity = findEntity(managerNo);
        apartmentManagerRepository.delete(entity);
    }

    private ApartmentManagerEntity findEntity(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));
    }

    private ApartmentEntity findOrCreateApartment(ApartmentManagerSignupRequestDto requestDto) {
        return apartmentRepository.findByName(requestDto.getApartmentName())
                .orElseGet(() -> apartmentRepository.save(ApartmentEntity.builder()
                        .name(requestDto.getApartmentName())
                        .password(generateApartmentPassword())
                        .address(requestDto.getAddress())
                        .detailAddress(requestDto.getDetailAddress())
                        .build()));
    }

    private String generateApartmentPassword() {
        String password;
        do {
            password = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        } while (apartmentRepository.existsByPassword(password));
        return password;
    }

    private String joinAddress(String address, String detailAddress) {
        if (isBlank(detailAddress)) {
            return address;
        }
        return address + " " + detailAddress;
    }

    private void validateDuplicate(ApartmentManagerSignupRequestDto requestDto) {
        if (apartmentManagerRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (apartmentManagerRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
    }

    private void validateSignupRequest(ApartmentManagerSignupRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가입 신청 정보를 입력해주세요.");
        }
        if (isBlank(requestDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디는 필수입니다.");
        }
        if (isBlank(requestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 필수입니다.");
        }
        if (isBlank(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일은 필수입니다.");
        }
        if (isBlank(requestDto.getPhone())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "전화번호는 필수입니다.");
        }
        if (isBlank(requestDto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 이름은 필수입니다.");
        }
        if (isBlank(requestDto.getApartmentName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아파트 이름은 필수입니다.");
        }
        if (isBlank(requestDto.getAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아파트 주소는 필수입니다.");
        }
        if (isBlank(requestDto.getDetailAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상세 주소는 필수입니다.");
        }
        if (isBlank(requestDto.getCareerImage())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경력증명서 사진은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
