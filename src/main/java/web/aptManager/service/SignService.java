package web.aptManager.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.dto.ApartmentManagerSignupRequestDto;
import web.aptManager.dto.SignDto;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.aptManager.repository.ApartmentRepository;
import web.common.file.FileService;
import web.common.type.ApprovalStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @Transactional
    public SignDto signup(ApartmentManagerSignupRequestDto requestDto) {
        validateSignupRequest(requestDto);
        validateDuplicate(requestDto);

        return saveManager(requestDto);
    }

    @Transactional
    public SignDto signup(ApartmentManagerSignupRequestDto requestDto, MultipartFile careerImage) {
        validateSignupRequestWithoutCareerImage(requestDto);
        validateDuplicate(requestDto);

        String savedFilePath = fileService.saveFile(careerImage);
        requestDto.setCareerImage(savedFilePath);

        return saveManager(requestDto);
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "apartment not found."));
            entity.setApartment(apartment);
        }

        if (signDto.getLoginId() != null && !signDto.getLoginId().equals(entity.getLoginId())) {
            if (apartmentManagerRepository.existsByLoginId(signDto.getLoginId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "loginId already exists.");
            }
            entity.setLoginId(signDto.getLoginId());
        }

        if (signDto.getEmail() != null && !signDto.getEmail().equals(entity.getEmail())) {
            if (apartmentManagerRepository.existsByEmail(signDto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists.");
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

    private SignDto saveManager(ApartmentManagerSignupRequestDto requestDto) {
        ApartmentEntity apartment = findOrCreateApartment(requestDto);

        ApartmentManagerEntity manager = ApartmentManagerEntity.builder()
                .apartment(apartment)
                .loginId(requestDto.getLoginId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .address(requestDto.getAddress())
                .name(requestDto.getName())
                .picture(requestDto.getCareerImage())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        ApartmentManagerEntity savedEntity = apartmentManagerRepository.save(manager);
        return savedEntity.toDTO();
    }

    private ApartmentManagerEntity findEntity(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "apartment manager not found."));
    }

    private ApartmentEntity findOrCreateApartment(ApartmentManagerSignupRequestDto requestDto) {
        return apartmentRepository.findByName(requestDto.getApartmentName())
                .orElseGet(() -> apartmentRepository.save(ApartmentEntity.builder()
                        .name(requestDto.getApartmentName())
                        .password(generateApartmentPassword())
                        .address(requestDto.getAddress())
                        .detailAddress(null)
                        .build()));
    }

    private String generateApartmentPassword() {
        String password;
        do {
            password = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        } while (apartmentRepository.existsByPassword(password));
        return password;
    }

    private void validateDuplicate(ApartmentManagerSignupRequestDto requestDto) {
        if (apartmentManagerRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "loginId already exists.");
        }
        if (apartmentManagerRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists.");
        }
    }

    private void validateSignupRequest(ApartmentManagerSignupRequestDto requestDto) {
        validateSignupRequestWithoutCareerImage(requestDto);
        if (isBlank(requestDto.getCareerImage())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "career image is required.");
        }
    }

    private void validateSignupRequestWithoutCareerImage(ApartmentManagerSignupRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "signup request is required.");
        }
        if (isBlank(requestDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "loginId is required.");
        }
        if (isBlank(requestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required.");
        }
        if (isBlank(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required.");
        }
        if (isBlank(requestDto.getPhone())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is required.");
        }
        if (isBlank(requestDto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required.");
        }
        if (isBlank(requestDto.getApartmentName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "apartmentName is required.");
        }
        if (isBlank(requestDto.getAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "address is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
