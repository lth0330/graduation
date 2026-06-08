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
import web.parking.dto.ParkingHistoryDto;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingHistoryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingHistoryQueryService {

    private final ParkingHistoryRepository parkingHistoryRepository;
    private final ApartmentManagerRepository apartmentManagerRepository;

    public ParkingHistoryDto findMyParkingHistory(Map<String, Object> principal, Integer historyId) {
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        ParkingHistoryEntity history = parkingHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주차 이력입니다."));

        Integer managerApartmentNo = getApartmentNo(manager);
        Integer historyApartmentNo = getHistoryApartmentNo(history);
        if (!managerApartmentNo.equals(historyApartmentNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 아파트의 주차 이력은 조회할 수 없습니다.");
        }

        return ParkingHistoryDto.from(history);
    }

    private ApartmentManagerEntity findManager(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));
    }

    private Integer getApartmentNo(ApartmentManagerEntity manager) {
        ApartmentEntity apartment = manager != null ? manager.getApartment() : null;
        if (apartment == null || apartment.getNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 아파트 정보가 없습니다.");
        }
        return apartment.getNo();
    }

    private Integer getHistoryApartmentNo(ParkingHistoryEntity history) {
        ParkingZoneEntity zone = history.getParkingZone();
        ParkingLotEntity parkingLot = zone != null ? zone.getParkingLot() : null;
        ApartmentEntity apartment = parkingLot != null ? parkingLot.getApartment() : null;
        if (apartment == null || apartment.getNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주차 이력의 아파트 정보가 없습니다.");
        }
        return apartment.getNo();
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
