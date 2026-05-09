package web.parking.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.parking.dto.ParkingLotDto;
import web.parking.dto.ParkingLotSaveRequestDto;
import web.parking.entity.ParkingLotEntity;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingLotManagementService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingZoneRepository parkingZoneRepository;
    private final ApartmentRepository apartmentRepository;

    public List<ParkingLotDto> findParkingLots(Integer apartmentNo) {
        return parkingLotRepository.findByApartment_No(apartmentNo)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ParkingLotDto create(ParkingLotSaveRequestDto requestDto) {
        validateSaveRequest(requestDto);
        ApartmentEntity apartment = apartmentRepository.findById(requestDto.getApartmentNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트입니다."));

        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .apartment(apartment)
                .name(requestDto.getName())
                .floor(requestDto.getFloor())
                .totalSpaces(requestDto.getTotalSpaces())
                .usedSpaces(requestDto.getUsedSpaces())
                .build();

        return toDto(parkingLotRepository.save(parkingLot));
    }

    @Transactional
    public void delete(Integer parkingLotNo) {
        ParkingLotEntity parkingLot = parkingLotRepository.findById(parkingLotNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주차장입니다."));
        parkingZoneRepository.deleteByParkingLot_No(parkingLotNo);
        parkingLotRepository.delete(parkingLot);
    }

    private void validateSaveRequest(ParkingLotSaveRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주차장 정보를 입력해주세요.");
        }
        if (requestDto.getApartmentNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아파트 번호는 필수입니다.");
        }
        if (isBlank(requestDto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주차장 이름은 필수입니다.");
        }
        if (isBlank(requestDto.getFloor())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "층 정보는 필수입니다.");
        }
        if (requestDto.getTotalSpaces() == null || requestDto.getTotalSpaces() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "전체 주차면 수는 1 이상이어야 합니다.");
        }
        if (requestDto.getUsedSpaces() == null || requestDto.getUsedSpaces() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용 중 주차면 수는 0 이상이어야 합니다.");
        }
        if (requestDto.getUsedSpaces() > requestDto.getTotalSpaces()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용 중 주차면 수는 전체 주차면 수보다 클 수 없습니다.");
        }
    }

    private ParkingLotDto toDto(ParkingLotEntity parkingLot) {
        return ParkingLotDto.builder()
                .parkingLotNo(parkingLot.getNo())
                .apartmentNo(parkingLot.getApartment() != null ? parkingLot.getApartment().getNo() : null)
                .name(parkingLot.getName())
                .floor(parkingLot.getFloor())
                .totalSpaces(parkingLot.getTotalSpaces())
                .usedSpaces(parkingLot.getUsedSpaces())
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
