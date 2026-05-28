package web.parking.service;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.parking.dto.VisitorCarManagementDto;
import web.resident.entity.ResidentEntity;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 아파트 관리자 화면에서 방문 차량(registered_cars) 목록을 조회한다.
public class VisitorCarManagementService {

    private final RegisteredCarRepository registeredCarRepository;

    public List<VisitorCarManagementDto> findVisitorCars(Integer apartmentNo) {
        return registeredCarRepository.findByResident_Apartment_No(apartmentNo)
                .stream()
                .map(this::toManagementDto)
                .toList();
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
                .parkedAt(visitorCar.getParkedAt())
                .expiresAt(visitorCar.getExpiresAt())
                .build();
    }
}
