package web.aptManager.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.dto.SignDto;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.aptManager.repository.ApartmentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ApartmentRepository apartmentRepository;

    @Transactional
    public SignDto signup(SignDto signDto) {
        validateRequired(signDto);
        validateDuplicate(signDto);

        ApartmentEntity apartment = apartmentRepository.findById(signDto.getApartmentNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트입니다."));

        ApartmentManagerEntity savedEntity = apartmentManagerRepository.save(signDto.toEntity(apartment));
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
            entity.setPassword(signDto.getPassword());
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

    private void validateDuplicate(SignDto signDto) {
        if (apartmentManagerRepository.existsByLoginId(signDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (apartmentManagerRepository.existsByEmail(signDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
    }

    private void validateRequired(SignDto signDto) {
        if (signDto.getApartmentNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아파트 번호는 필수입니다.");
        }
        if (isBlank(signDto.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디는 필수입니다.");
        }
        if (isBlank(signDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 필수입니다.");
        }
        if (isBlank(signDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일은 필수입니다.");
        }
        if (isBlank(signDto.getPicture())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "재직증명서 사진은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
