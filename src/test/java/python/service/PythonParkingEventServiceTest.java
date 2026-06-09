package python.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.repository.RegisteredCarRepository;
import app.service.AppResidentFeatureService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import python.dto.PythonParkingEntryRequestDto;
import python.dto.PythonParkingPlateUpdateRequestDto;
import web.common.file.ParkingSnapshotStorageService;
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;

class PythonParkingEventServiceTest {

    @Test
    void saveEntryStoresImagePathCreatedFromImageBase64() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("empty")
                .zoneType("normal")
                .parkingLot(ParkingLotEntity.builder().no(1).build())
                .build();
        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(zone));
        when(fixture.snapshotStorageService.saveBase64Image("base64-image"))
                .thenReturn("/uploads/parking-snapshots/snapshot.jpg");
        when(fixture.parkingHistoryRepository.save(any(ParkingHistoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PythonParkingEntryRequestDto requestDto = new PythonParkingEntryRequestDto();
        requestDto.setZone("a-b1-001");
        requestDto.setPlate("37나5209");
        requestDto.setEntryTime("2026-06-09 10:30:00");
        requestDto.setImageBase64("base64-image");

        fixture.service.saveEntry(requestDto);

        ArgumentCaptor<ParkingHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ParkingHistoryEntity.class);
        verify(fixture.parkingHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getImagePath())
                .isEqualTo("/uploads/parking-snapshots/snapshot.jpg");
    }

    @Test
    void updatePlateStoresImagePathCreatedFromImageBase64OnActiveHistory() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("occupied")
                .zoneType("normal")
                .build();
        ParkingHistoryEntity activeHistory = ParkingHistoryEntity.builder()
                .parkingZone(zone)
                .zoneSnapshot("a-b1-001")
                .plate("UNKNOWN")
                .status("PARKED")
                .parkType("normal")
                .build();
        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(zone));
        when(fixture.parkingHistoryRepository.findFirstByParkingZone_NoAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(
                1,
                "PARKED"
        )).thenReturn(Optional.of(activeHistory));
        when(fixture.snapshotStorageService.saveBase64Image("base64-image"))
                .thenReturn("/uploads/parking-snapshots/update.jpg");

        PythonParkingPlateUpdateRequestDto requestDto = new PythonParkingPlateUpdateRequestDto();
        requestDto.setZone("a-b1-001");
        requestDto.setPlate("37나5209");
        requestDto.setImageBase64("base64-image");

        fixture.service.updatePlate(requestDto);

        assertThat(activeHistory.getPlate()).isEqualTo("37나5209");
        assertThat(activeHistory.getImagePath()).isEqualTo("/uploads/parking-snapshots/update.jpg");
    }

    private static class ServiceFixture {
        private final ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        private final ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        private final ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        private final ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        private final RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        private final ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        private final AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);
        private final ParkingSnapshotStorageService snapshotStorageService = mock(ParkingSnapshotStorageService.class);

        private final PythonParkingEventService service = new PythonParkingEventService(
                parkingZoneRepository,
                parkingLotRepository,
                parkingHistoryRepository,
                residentVehicleRepository,
                registeredCarRepository,
                managerNotificationService,
                appResidentFeatureService,
                snapshotStorageService
        );
    }
}
