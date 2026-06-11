package python.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.entity.RegisteredCarEntity;
import app.repository.AppNotificationRepository;
import app.repository.RegisteredCarRepository;
import app.service.AppResidentFeatureService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import python.repository.GateEntryLogRepository;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.notification.entity.ManagerNotificationEntity;
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;

class PythonGateServiceTest {

    @Test
    void checkPlateCalculatesOccupancyWithNormalZonesOnly() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .gateOccupancyBlockEnabled(true)
                .gateForceOpenEnabled(false)
                .build();
        ResidentEntity resident = ResidentEntity.builder().apartment(apartment).build();
        RegisteredCarEntity visitorVehicle = RegisteredCarEntity.builder()
                .number("12가1234")
                .resident(resident)
                .build();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .apartment(apartment)
                .totalSpaces(5)
                .usedSpaces(4)
                .build();

        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(registeredCarRepository.findFirstByNumber("12가1234")).thenReturn(Optional.of(visitorVehicle));
        when(parkingLotRepository.findByApartment_No(1)).thenReturn(List.of(parkingLot));
        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(
                normalZone(parkingLot, "a-b1-001", "occupied"),
                normalZone(parkingLot, "a-b1-002", "occupied"),
                normalZone(parkingLot, "a-b1-003", "occupied"),
                normalZone(parkingLot, "a-b1-004", "empty"),
                doubleLaneZone(parkingLot, "a-b1-007", "occupied")
        ));

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        Map<String, Object> result = service.checkPlate("12가1234", 1);

        assertThat(result.get("total")).isEqualTo(4);
        assertThat(result.get("used")).isEqualTo(3);
        assertThat(result.get("rate")).isEqualTo(0.75);
        assertThat(result.get("gate_open")).isEqualTo(true);
    }

    @Test
    void checkPlateBlocksVisitorWhenNormalZoneOccupancyIsOverEightyPercent() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .gateOccupancyBlockEnabled(true)
                .gateForceOpenEnabled(false)
                .build();
        ResidentEntity resident = ResidentEntity.builder().apartment(apartment).build();
        RegisteredCarEntity visitorVehicle = RegisteredCarEntity.builder()
                .number("12가1234")
                .resident(resident)
                .build();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder()
                .no(1)
                .apartment(apartment)
                .totalSpaces(6)
                .usedSpaces(5)
                .build();

        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(registeredCarRepository.findFirstByNumber("12가1234")).thenReturn(Optional.of(visitorVehicle));
        when(parkingLotRepository.findByApartment_No(1)).thenReturn(List.of(parkingLot));
        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(
                normalZone(parkingLot, "a-b1-001", "occupied"),
                normalZone(parkingLot, "a-b1-002", "occupied"),
                normalZone(parkingLot, "a-b1-003", "occupied"),
                normalZone(parkingLot, "a-b1-004", "occupied"),
                normalZone(parkingLot, "a-b1-005", "occupied"),
                normalZone(parkingLot, "a-b1-006", "empty"),
                doubleLaneZone(parkingLot, "a-b1-007", "occupied")
        ));

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        Map<String, Object> result = service.checkPlate("12가1234", 1);

        assertThat(result.get("total")).isEqualTo(6);
        assertThat(result.get("used")).isEqualTo(5);
        assertThat(result.get("rate")).isEqualTo(5.0 / 6.0);
        assertThat(result.get("gate_open")).isEqualTo(false);
        assertThat(result.get("reason")).isEqualTo("주차장 점유율이 80% 이상이라 방문차량은 입차할 수 없습니다.");
    }

    @Test
    void checkPlateRemovesWhitespaceBeforeMatchingAndResponding() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .build();
        ResidentEntity resident = ResidentEntity.builder().no(7).name("차량소유자").apartment(apartment).build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .number("37나5209")
                .resident(resident)
                .build();

        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(residentVehicleRepository.findByNumber("37나5209")).thenReturn(Optional.of(vehicle));

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        Map<String, Object> result = service.checkPlate("37나 5209", 1);

        assertThat(result.get("plate")).isEqualTo("37나5209");
        assertThat(result.get("gate_open")).isEqualTo(true);
        verify(residentVehicleRepository).findByNumber("37나5209");
    }

    @Test
    void checkPlateSendsGateEntryNotificationToResidentVehicleOwner() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .build();
        ResidentEntity resident = ResidentEntity.builder().no(7).name("차량소유자").apartment(apartment).build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .number("37나5209")
                .resident(resident)
                .build();

        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(residentVehicleRepository.findByNumber("37나5209")).thenReturn(Optional.of(vehicle));

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        service.checkPlate("37나5209", 1);

        verify(appResidentFeatureService).sendPushToResident(
                7,
                "🚗 입차 알림",
                "37나5209 차량이 입구를 통과했습니다."
        );
    }

    @Test
    void saveGateLogStartsVisitorExpirationWithoutMarkingParked() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);
        RegisteredCarEntity visitorVehicle = RegisteredCarEntity.builder()
                .number("12가1234")
                .build();
        LocalDateTime gateTime = LocalDateTime.of(2026, 6, 11, 9, 15);

        when(registeredCarRepository.findFirstByNumber("12가1234")).thenReturn(Optional.of(visitorVehicle));
        when(gateEntryLogRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        service.saveGateLog(Map.of(
                "c_number", "12가1234",
                "is_resident", true,
                "gate_open", true,
                "gate_time", "2026-06-11 09:15:00"
        ));

        assertThat(visitorVehicle.getParkedAt()).isNull();
        assertThat(visitorVehicle.getExpiresAt()).isEqualTo(gateTime.plusDays(1));
    }

    @Test
    void saveDoubleParkingAlertSavesOcrErrorWithParkingHistoryReference() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .build();
        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(managerNotificationService.createApartmentNotification(
                eq(apartment),
                eq("ocr_error"),
                eq("번호판 인식 실패"),
                contains("A1 구역 번호판 인식 실패"),
                eq("parking_history"),
                eq(123)
        )).thenReturn(ManagerNotificationEntity.builder().no(10).apartment(apartment).build());

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        service.saveDoubleParkingAlert(Map.of(
                "type", "ocr_error",
                "zone", "A1",
                "history_id", 123,
                "apartment_no", 1,
                "image_path", "C:\\snapshots\\A1.jpg"
        ));

        verify(managerNotificationService).createApartmentNotification(
                eq(apartment),
                eq("ocr_error"),
                eq("번호판 인식 실패"),
                contains("A1 구역 번호판 인식 실패"),
                eq("parking_history"),
                eq(123)
        );
    }

    @Test
    void saveDoubleParkingAlertUsesStableReferenceForAssignFailToPreventDuplicates() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        AppResidentFeatureService appResidentFeatureService = mock(AppResidentFeatureService.class);

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .build();
        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(managerNotificationService.createApartmentNotification(
                eq(apartment),
                eq("assign_fail"),
                eq("번호판 자동 연결 실패"),
                contains("후보 차량"),
                eq("gate_alert"),
                anyInt()
        )).thenReturn(ManagerNotificationEntity.builder().no(10).apartment(apartment).build());

        PythonGateService service = new PythonGateService(
                residentVehicleRepository,
                registeredCarRepository,
                parkingHistoryRepository,
                gateEntryLogRepository,
                parkingLotRepository,
                parkingZoneRepository,
                apartmentRepository,
                managerNotificationService,
                appResidentFeatureService,
                mock(AppNotificationRepository.class)
        );

        service.saveDoubleParkingAlert(Map.of(
                "type", "assign_fail",
                "apartment_no", 1,
                "candidates", "12가1234,12가1236"
        ));

        verify(managerNotificationService).createApartmentNotification(
                eq(apartment),
                eq("assign_fail"),
                eq("번호판 자동 연결 실패"),
                contains("후보 차량"),
                eq("gate_alert"),
                anyInt()
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
