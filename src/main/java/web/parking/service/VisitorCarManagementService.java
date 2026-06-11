package web.parking.service;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import python.repository.GateEntryLogRepository;
import web.parking.dto.VisitorCarManagementDto;
import web.resident.entity.ResidentEntity;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 아파트 관리자 화면에서 방문 차량(registered_cars) 목록을 조회한다.
public class VisitorCarManagementService {

    private final RegisteredCarRepository registeredCarRepository;
    private final GateEntryLogRepository gateEntryLogRepository;

    public List<VisitorCarManagementDto> findVisitorCars(Integer apartmentNo) {
        return registeredCarRepository.findByResident_Apartment_No(apartmentNo)
                .stream()
                .map(this::toManagementDto)
                .toList();
    }

    @Transactional
    public VisitorCarManagementDto updateExpiresAt(Integer visitorCarNo, Integer apartmentNo, LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료시간은 필수입니다.");
        }

        RegisteredCarEntity visitorCar = findEntity(visitorCarNo);
        validateApartment(visitorCar, apartmentNo);
        visitorCar.setExpiresAt(expiresAt);
        return toManagementDto(visitorCar);
    }

    @Transactional
    public void delete(Integer visitorCarNo, Integer apartmentNo) {
        RegisteredCarEntity visitorCar = findEntity(visitorCarNo);
        validateApartment(visitorCar, apartmentNo);
        registeredCarRepository.delete(visitorCar);
    }

    private RegisteredCarEntity findEntity(Integer visitorCarNo) {
        return registeredCarRepository.findById(visitorCarNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "방문 차량을 찾을 수 없습니다."));
    }

    private void validateApartment(RegisteredCarEntity visitorCar, Integer apartmentNo) {
        Integer ownerApartmentNo = visitorCar.getResident() != null
                && visitorCar.getResident().getApartment() != null
                ? visitorCar.getResident().getApartment().getNo()
                : null;
        if (apartmentNo == null || ownerApartmentNo == null || !apartmentNo.equals(ownerApartmentNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근할 수 없는 방문 차량입니다.");
        }
    }

    private VisitorCarManagementDto toManagementDto(RegisteredCarEntity visitorCar) {
        ResidentEntity resident = visitorCar.getResident();

        return VisitorCarManagementDto.builder()
                .visitorCarNo(visitorCar.getNo())
                .carNumber(visitorCar.getNumber())
                .ownerId(resident != null ? resident.getNo() : null)
                .ownerName(resident != null ? resident.getName() : null)
                .building(resident != null ? resident.getDong() : null)
                .unit(resident != null ? resident.getHo() : null)
                .registeredAt(visitorCar.getRegisteredAt())
                .gateEnteredAt(findLatestGateEnteredAt(visitorCar))
                .parkedAt(visitorCar.getParkedAt())
                .expiresAt(visitorCar.getExpiresAt())
                .build();
    }

    private LocalDateTime findLatestGateEnteredAt(RegisteredCarEntity visitorCar) {
        if (visitorCar == null || visitorCar.getNumber() == null || visitorCar.getNumber().isBlank()) {
            return null;
        }
        return gateEntryLogRepository.findFirstByPlateAndGateOpenTrueOrderByGateTimeDesc(visitorCar.getNumber())
                .map(gateEntryLog -> gateEntryLog.getGateTime())
                .orElse(null);
    }
}
