package web.parking.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.parking.dto.GatePolicyDto;
import web.parking.dto.GatePolicyRequestDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatePolicyService {

    private final ApartmentManagerRepository apartmentManagerRepository;

    public GatePolicyDto findMyPolicy(Map<String, Object> principal) {
        return toDto(findMyApartment(principal));
    }

    @Transactional
    public GatePolicyDto updateMyPolicy(Map<String, Object> principal, GatePolicyRequestDto requestDto) {
        if (requestDto == null
                || (requestDto.getGateOccupancyBlockEnabled() == null
                && requestDto.getGateForceOpenEnabled() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차단기 정책 설정값은 필수입니다.");
        }

        ApartmentEntity apartment = findMyApartment(principal);
        if (requestDto.getGateOccupancyBlockEnabled() != null) {
            apartment.setGateOccupancyBlockEnabled(requestDto.getGateOccupancyBlockEnabled());
        }
        if (requestDto.getGateForceOpenEnabled() != null) {
            apartment.setGateForceOpenEnabled(requestDto.getGateForceOpenEnabled());
        }
        return toDto(apartment);
    }

    private ApartmentEntity findMyApartment(Map<String, Object> principal) {
        ApartmentManagerEntity manager = apartmentManagerRepository.findById(getInteger(principal, "userNo"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));

        ApartmentEntity apartment = manager.getApartment();
        if (apartment == null || apartment.getNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 아파트 정보가 없습니다.");
        }
        return apartment;
    }

    private GatePolicyDto toDto(ApartmentEntity apartment) {
        return GatePolicyDto.builder()
                .apartmentNo(apartment.getNo())
                .gateOccupancyBlockEnabled(isOccupancyBlockEnabled(apartment))
                .gateForceOpenEnabled(isForceOpenEnabled(apartment))
                .build();
    }

    private boolean isOccupancyBlockEnabled(ApartmentEntity apartment) {
        return apartment.getGateOccupancyBlockEnabled() == null || apartment.getGateOccupancyBlockEnabled();
    }

    private boolean isForceOpenEnabled(ApartmentEntity apartment) {
        return apartment.getGateForceOpenEnabled() != null && apartment.getGateForceOpenEnabled();
    }

    private Integer getInteger(Map<String, Object> principal, String key) {
        Object value = principal != null ? principal.get(key) : null;
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }
}
