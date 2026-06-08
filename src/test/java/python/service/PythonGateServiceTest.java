package python.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.repository.RegisteredCarRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import python.repository.GateEntryLogRepository;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.notification.entity.ManagerNotificationEntity;
import web.notification.service.ManagerNotificationService;
import web.parking.repository.ParkingHistoryRepository;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;

class PythonGateServiceTest {

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

        ApartmentEntity apartment = ApartmentEntity.builder()
                .no(1)
                .name("테스트아파트")
                .build();
        when(apartmentRepository.findById(1)).thenReturn(Optional.of(apartment));
        when(managerNotificationService.createApartmentNotification(
                eq(apartment),
                eq("ocr_error"),
                eq("번호판 인식 실패"),
                contains("A1 구역에서 번호판을 인식하지 못했습니다."),
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
                managerNotificationService
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
                contains("A1 구역에서 번호판을 인식하지 못했습니다."),
                eq("parking_history"),
                eq(123)
        );
    }
}
