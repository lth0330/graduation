package web.parking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import app.repository.AppNotificationRepository;
import app.repository.AppSettingRepository;
import app.repository.DeviceInfoRepository;
import app.repository.WaitingListRepository;
import app.service.FcmService;
import web.parking.dto.ParkingZoneDto;
import web.parking.dto.ParkingZoneLayoutRequestDto;
import web.parking.dto.ParkingZoneSaveRequestDto;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;

class ParkingZoneManagementServiceTest {

    @Test
    void createUsesDefaultLayoutSizeWhenWidthAndHeightAreMissing() {
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(1).build();
        ParkingZoneSaveRequestDto request = new ParkingZoneSaveRequestDto();
        request.setParkingLotNo(1);
        request.setAreaNumber("A1");
        request.setLocation("B1");
        request.setLayoutRow(1);
        request.setLayoutColumn(1);

        when(parkingLotRepository.findById(1)).thenReturn(Optional.of(parkingLot));
        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of());
        when(parkingZoneRepository.save(any(ParkingZoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingZoneManagementService service = createService(parkingZoneRepository, parkingLotRepository);

        ParkingZoneDto result = service.create(request);

        assertThat(result.getLayoutWidth()).isEqualTo(2);
        assertThat(result.getLayoutHeight()).isEqualTo(1);
    }

    @Test
    void findParkingZonesIncludesCurrentCarNumber() {
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(1).build();
        ParkingZoneEntity occupiedZone = ParkingZoneEntity.builder()
                .no(10)
                .parkingLot(parkingLot)
                .areaNumber("A1")
                .location("B1")
                .status("occupied")
                .layoutRow(1)
                .layoutColumn(1)
                .layoutWidth(1)
                .layoutHeight(2)
                .currentCarNumber("12가3456")
                .build();

        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(occupiedZone));

        ParkingZoneManagementService service = createService(parkingZoneRepository, parkingLotRepository);

        List<ParkingZoneDto> result = service.findParkingZones(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentCarNumber()).isEqualTo("12가3456");
    }

    @Test
    void createRejectsOverlappingLayoutArea() {
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(1).build();
        ParkingZoneEntity existing = ParkingZoneEntity.builder()
                .no(10)
                .parkingLot(parkingLot)
                .areaNumber("A1")
                .location("B1")
                .layoutRow(1)
                .layoutColumn(1)
                .layoutWidth(2)
                .layoutHeight(1)
                .build();
        ParkingZoneSaveRequestDto request = new ParkingZoneSaveRequestDto();
        request.setParkingLotNo(1);
        request.setAreaNumber("A2");
        request.setLocation("B1");
        request.setLayoutRow(1);
        request.setLayoutColumn(2);
        request.setLayoutWidth(2);
        request.setLayoutHeight(1);

        when(parkingLotRepository.findById(1)).thenReturn(Optional.of(parkingLot));
        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(existing));

        ParkingZoneManagementService service = createService(parkingZoneRepository, parkingLotRepository);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateLayoutStoresSizeAndRejectsOverlapWithOtherZone() {
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(1).build();
        ParkingZoneEntity target = ParkingZoneEntity.builder()
                .no(10)
                .parkingLot(parkingLot)
                .areaNumber("A1")
                .location("B1")
                .layoutRow(1)
                .layoutColumn(1)
                .layoutWidth(2)
                .layoutHeight(1)
                .build();
        ParkingZoneEntity other = ParkingZoneEntity.builder()
                .no(11)
                .parkingLot(parkingLot)
                .areaNumber("A2")
                .location("B1")
                .layoutRow(2)
                .layoutColumn(2)
                .layoutWidth(2)
                .layoutHeight(1)
                .build();
        ParkingZoneLayoutRequestDto request = new ParkingZoneLayoutRequestDto();
        request.setLayoutRow(1);
        request.setLayoutColumn(2);
        request.setLayoutWidth(2);
        request.setLayoutHeight(2);

        when(parkingZoneRepository.findById(10)).thenReturn(Optional.of(target));
        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(target, other));

        ParkingZoneManagementService service = createService(parkingZoneRepository, parkingLotRepository);

        assertThatThrownBy(() -> service.updateLayout(10, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private ParkingZoneManagementService createService(
            ParkingZoneRepository parkingZoneRepository,
            ParkingLotRepository parkingLotRepository
    ) {
        return new ParkingZoneManagementService(
                parkingZoneRepository,
                parkingLotRepository,
                mock(WaitingListRepository.class),
                mock(AppNotificationRepository.class),
                mock(AppSettingRepository.class),
                mock(DeviceInfoRepository.class),
                mock(FcmService.class)
        );
    }
}
