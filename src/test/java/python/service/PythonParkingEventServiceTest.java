package python.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.repository.RegisteredCarRepository;
import app.service.AppResidentFeatureService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import python.dto.PythonParkingEntryRequestDto;
import python.dto.PythonParkingPlateUpdateRequestDto;
import python.service.PlateCorrectionReviewService;
import web.common.file.ParkingSnapshotStorageService;
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;

class PythonParkingEventServiceTest {

    @Test
    void findOccupancyCalculatesNormalZonesOnly() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .totalSpaces(5)
                .usedSpaces(4)
                .build();

        when(fixture.parkingLotRepository.findAll()).thenReturn(List.of(parkingLot));
        when(fixture.parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(
                normalZone(parkingLot, "a-b1-001", "occupied"),
                normalZone(parkingLot, "a-b1-002", "occupied"),
                normalZone(parkingLot, "a-b1-003", "occupied"),
                normalZone(parkingLot, "a-b1-004", "empty"),
                doubleLaneZone(parkingLot, "a-b1-007", "occupied")
        ));

        Map<String, Object> result = fixture.service.findOccupancy();

        assertThat(result.get("total")).isEqualTo(4);
        assertThat(result.get("used")).isEqualTo(3);
        assertThat(result.get("available")).isEqualTo(1);
        assertThat(result.get("rate")).isEqualTo(0.75);
    }

    @Test
    void saveEntryRemovesPlateWhitespaceBeforeSavingAndMatching() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("empty")
                .zoneType("normal")
                .parkingLot(ParkingLotEntity.builder().no(1).usedSpaces(0).build())
                .build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .number("37나5209")
                .build();

        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(zone));
        when(fixture.residentVehicleRepository.findByNumber("37나5209")).thenReturn(Optional.of(vehicle));
        when(fixture.parkingHistoryRepository.save(any(ParkingHistoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PythonParkingEntryRequestDto requestDto = new PythonParkingEntryRequestDto();
        requestDto.setZone("a-b1-001");
        requestDto.setPlate("37나 5209");

        fixture.service.saveEntry(requestDto);

        ArgumentCaptor<ParkingHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ParkingHistoryEntity.class);
        verify(fixture.parkingHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPlate()).isEqualTo("37나5209");
        assertThat(historyCaptor.getValue().getResidentVehicle()).isEqualTo(vehicle);
        assertThat(zone.getCurrentCarNumber()).isEqualTo("37나5209");
        verify(fixture.residentVehicleRepository).findByNumber("37나5209");
    }

    @Test
    void saveEntrySynchronizesParkingLotUsedSpacesFromOccupiedZones() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .usedSpaces(0)
                .build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("empty")
                .zoneType("normal")
                .parkingLot(parkingLot)
                .build();

        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(zone));
        when(fixture.parkingZoneRepository.countByParkingLot_NoAndZoneTypeAndStatus(
                1,
                "normal",
                "occupied"
        )).thenReturn(3L);
        when(fixture.parkingHistoryRepository.save(any(ParkingHistoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PythonParkingEntryRequestDto requestDto = new PythonParkingEntryRequestDto();
        requestDto.setZone("a-b1-001");
        requestDto.setPlate("37나5209");

        fixture.service.saveEntry(requestDto);

        assertThat(parkingLot.getUsedSpaces()).isEqualTo(3);
    }

    @Test
    void saveEntryMarksLinkedZoneOccupiedForMultiZoneParking() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .usedSpaces(0)
                .build();
        ParkingZoneEntity mainZone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("empty")
                .zoneType("normal")
                .parkingLot(parkingLot)
                .build();
        ParkingZoneEntity linkedZone = ParkingZoneEntity.builder()
                .no(2)
                .areaNumber("a-b1-002")
                .status("empty")
                .zoneType("normal")
                .parkingLot(parkingLot)
                .build();

        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(mainZone));
        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-002")).thenReturn(Optional.of(linkedZone));
        when(fixture.parkingZoneRepository.countByParkingLot_NoAndZoneTypeAndStatus(
                1,
                "normal",
                "occupied"
        )).thenReturn(2L);
        when(fixture.parkingHistoryRepository.save(any(ParkingHistoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PythonParkingEntryRequestDto requestDto = new PythonParkingEntryRequestDto();
        requestDto.setZone("a-b1-001");
        requestDto.setPlate("37나5209");
        requestDto.setParkType("multi_zone");
        requestDto.setLinkedZone("a-b1-002");

        fixture.service.saveEntry(requestDto);

        assertThat(mainZone.getStatus()).isEqualTo("occupied");
        assertThat(linkedZone.getStatus()).isEqualTo("occupied");
        assertThat(linkedZone.getCurrentCarNumber()).isEqualTo("37나5209");
        assertThat(parkingLot.getUsedSpaces()).isEqualTo(2);
        verify(fixture.appResidentFeatureService).updateParking(argThat(request ->
                request.getUpdates() != null && request.getUpdates().size() == 2
        ));
    }

    @Test
    void saveEntryStoresImagePathCreatedFromImageBase64() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("empty")
                .zoneType("normal")
                .parkingLot(ParkingLotEntity.builder().no(1).usedSpaces(0).build())
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
    void saveEntryRecordsPlateCorrectionReviewWhenPythonMarksNeedsReview() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .usedSpaces(0)
                .apartment(web.aptManager.entity.ApartmentEntity.builder().no(1).name("테스트아파트").build())
                .build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("empty")
                .zoneType("normal")
                .parkingLot(parkingLot)
                .build();
        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(zone));
        when(fixture.parkingHistoryRepository.save(any(ParkingHistoryEntity.class)))
                .thenAnswer(invocation -> {
                    ParkingHistoryEntity history = invocation.getArgument(0);
                    history.setId(11);
                    fixture.savedHistory = history;
                    return history;
                });

        PythonParkingEntryRequestDto requestDto = new PythonParkingEntryRequestDto();
        requestDto.setZone("a-b1-001");
        requestDto.setPlate(null);
        requestDto.setOcrPlate("12가1235");
        requestDto.setMatchedPlate(null);
        requestDto.setCandidateList(List.of("12가1234", "12가1236"));
        requestDto.setDistance(1);
        requestDto.setNeedsReview(true);

        fixture.service.saveEntry(requestDto);

        verify(fixture.plateCorrectionReviewService).recordNeedsReview(
                parkingLot.getApartment(),
                fixture.savedHistory,
                "a-b1-001",
                "12가1235",
                null,
                List.of("12가1234", "12가1236"),
                1
        );
    }

    @Test
    void saveExitClearsLinkedZoneAndSynchronizesParkingLotUsedSpaces() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .usedSpaces(2)
                .build();
        ParkingZoneEntity mainZone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-001")
                .status("occupied")
                .zoneType("normal")
                .parkingLot(parkingLot)
                .currentCarNumber("37나5209")
                .build();
        ParkingZoneEntity linkedZone = ParkingZoneEntity.builder()
                .no(2)
                .areaNumber("a-b1-002")
                .status("occupied")
                .zoneType("normal")
                .parkingLot(parkingLot)
                .currentCarNumber("37나5209")
                .build();
        ParkingHistoryEntity activeHistory = ParkingHistoryEntity.builder()
                .parkingZone(mainZone)
                .zoneSnapshot("a-b1-001")
                .plate("37나5209")
                .status("PARKED")
                .parkType("multi_zone")
                .linkedZone("a-b1-002")
                .build();

        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-001")).thenReturn(Optional.of(mainZone));
        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-002")).thenReturn(Optional.of(linkedZone));
        when(fixture.parkingHistoryRepository.findFirstByParkingZone_NoAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(
                1,
                "PARKED"
        )).thenReturn(Optional.of(activeHistory));
        when(fixture.parkingZoneRepository.countByParkingLot_NoAndZoneTypeAndStatus(
                1,
                "normal",
                "occupied"
        )).thenReturn(0L);

        python.dto.PythonParkingExitRequestDto requestDto = new python.dto.PythonParkingExitRequestDto();
        requestDto.setZone("a-b1-001");

        fixture.service.saveExit(requestDto);

        assertThat(mainZone.getStatus()).isEqualTo("empty");
        assertThat(linkedZone.getStatus()).isEqualTo("empty");
        assertThat(linkedZone.getCurrentCarNumber()).isNull();
        assertThat(parkingLot.getUsedSpaces()).isEqualTo(0);
        verify(fixture.appResidentFeatureService).updateParking(argThat(request ->
                request.getUpdates() != null && request.getUpdates().size() == 2
        ));
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

    @Test
    void updatePlateSendsParkingNotificationToResidentVehicleOwner() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(1)
                .areaNumber("a-b1-007")
                .status("occupied")
                .zoneType("normal")
                .build();
        ParkingHistoryEntity activeHistory = ParkingHistoryEntity.builder()
                .parkingZone(zone)
                .zoneSnapshot("a-b1-007")
                .plate("UNKNOWN")
                .status("PARKED")
                .parkType("normal")
                .build();
        ResidentEntity resident = ResidentEntity.builder().no(7).name("차량소유자").build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .number("37나5209")
                .resident(resident)
                .build();

        when(fixture.parkingZoneRepository.findByAreaNumber("a-b1-007")).thenReturn(Optional.of(zone));
        when(fixture.parkingHistoryRepository.findFirstByParkingZone_NoAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(
                1,
                "PARKED"
        )).thenReturn(Optional.of(activeHistory));
        when(fixture.residentVehicleRepository.findByNumber("37나5209")).thenReturn(Optional.of(vehicle));

        PythonParkingPlateUpdateRequestDto requestDto = new PythonParkingPlateUpdateRequestDto();
        requestDto.setZone("a-b1-007");
        requestDto.setPlate("37나5209");

        fixture.service.updatePlate(requestDto);

        verify(fixture.appResidentFeatureService).sendPushToResident(
                7,
                "🅿️ 주차 완료 알림",
                "[a-b1-007] 구역에 차량(37나5209) 주차가 완료되었습니다."
        );
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
        private final PlateCorrectionReviewService plateCorrectionReviewService = mock(PlateCorrectionReviewService.class);
        private ParkingHistoryEntity savedHistory;

        private final PythonParkingEventService service = new PythonParkingEventService(
                parkingZoneRepository,
                parkingLotRepository,
                parkingHistoryRepository,
                residentVehicleRepository,
                registeredCarRepository,
                managerNotificationService,
                appResidentFeatureService,
                snapshotStorageService,
                plateCorrectionReviewService
        );
    }

    private ParkingZoneEntity normalZone(ParkingLotEntity parkingLot, String areaNumber, String status) {
        return ParkingZoneEntity.builder()
                .parkingLot(parkingLot)
                .areaNumber(areaNumber)
                .zoneType("normal")
                .status(status)
                .build();
    }

    private ParkingZoneEntity doubleLaneZone(ParkingLotEntity parkingLot, String areaNumber, String status) {
        return ParkingZoneEntity.builder()
                .parkingLot(parkingLot)
                .areaNumber(areaNumber)
                .zoneType("double_lane")
                .status(status)
                .build();
    }
}
