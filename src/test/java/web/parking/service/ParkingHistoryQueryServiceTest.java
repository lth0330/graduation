package web.parking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.parking.dto.ParkingHistoryDto;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingHistoryRepository;

class ParkingHistoryQueryServiceTest {

    @Test
    void findMyParkingHistoryReturnsHistoryForSameApartmentManager() {
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(3).apartment(apartment).build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(5)
                .parkingLot(parkingLot)
                .areaNumber("A1")
                .build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("A1")
                .plate("UNKNOWN")
                .entryTime(LocalDateTime.of(2026, 6, 8, 10, 30))
                .status("PARKED")
                .parkType("normal")
                .imagePath("C:\\snapshots\\A1.jpg")
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(parkingHistoryRepository.findById(11)).thenReturn(Optional.of(history));

        ParkingHistoryQueryService service = new ParkingHistoryQueryService(
                parkingHistoryRepository,
                apartmentManagerRepository
        );

        ParkingHistoryDto result = service.findMyParkingHistory(Map.of("userNo", 7), 11);

        assertThat(result.getHistoryId()).isEqualTo(11);
        assertThat(result.getApartmentNo()).isEqualTo(1);
        assertThat(result.getParkingLotNo()).isEqualTo(3);
        assertThat(result.getParkingZoneNo()).isEqualTo(5);
        assertThat(result.getZone()).isEqualTo("A1");
        assertThat(result.getPlate()).isEqualTo("UNKNOWN");
        assertThat(result.getImagePath()).isEqualTo("C:\\snapshots\\A1.jpg");
    }

    @Test
    void findMyParkingHistoryRejectsDifferentApartmentHistory() {
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ApartmentEntity managerApartment = ApartmentEntity.builder().no(1).name("관리자아파트").build();
        ApartmentEntity otherApartment = ApartmentEntity.builder().no(2).name("다른아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(managerApartment).build();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(3).apartment(otherApartment).build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(5)
                .parkingLot(parkingLot)
                .areaNumber("B1")
                .build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("B1")
                .plate("UNKNOWN")
                .entryTime(LocalDateTime.of(2026, 6, 8, 10, 30))
                .status("PARKED")
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(parkingHistoryRepository.findById(11)).thenReturn(Optional.of(history));

        ParkingHistoryQueryService service = new ParkingHistoryQueryService(
                parkingHistoryRepository,
                apartmentManagerRepository
        );

        assertThatThrownBy(() -> service.findMyParkingHistory(Map.of("userNo", 7), 11))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
