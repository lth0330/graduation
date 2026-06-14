package app.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.entity.AppNotificationEntity;
import app.entity.DeviceInfoEntity;
import app.repository.AppNotificationRepository;
import app.repository.AppSettingRepository;
import app.repository.DeviceInfoRepository;
import app.repository.RegisteredCarRepository;
import app.repository.WaitingListRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import web.inquiry.repository.ResidentInquiryRepository;
import web.notification.service.ManagerNotificationService;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

class AppResidentFeatureServiceTest {

    @Test
    void sendPushToResidentSkipsUnreadDuplicateNotification() {
        Fixture fixture = new Fixture();
        ResidentEntity resident = ResidentEntity.builder().no(7).name("입주민").build();
        String title = "🚨 이중주차 알림";
        String body = "[a-b1-007] 구역에 이중주차가 확인되었습니다. 이동 주차 바랍니다.";

        when(fixture.residentRepository.findById(7)).thenReturn(Optional.of(resident));
        when(fixture.notificationRepository.findByResident_NoOrderByCreatedAtDesc(7)).thenReturn(List.of(
                AppNotificationEntity.builder()
                        .resident(resident)
                        .type("parking")
                        .title(title)
                        .message(body)
                        .read(false)
                        .build()
        ));
        when(fixture.deviceInfoRepository.findByResident_No(7)).thenReturn(List.of(
                DeviceInfoEntity.builder().fcmToken("token-1").resident(resident).build()
        ));

        fixture.service.sendPushToResident(7, title, body);

        verify(fixture.notificationRepository, never()).save(any(AppNotificationEntity.class));
        verify(fixture.fcmService, never()).sendPush(any(), any(), any());
    }

    @Test
    void sendPushToResidentSavesAndPushesWhenDuplicateDoesNotExist() {
        Fixture fixture = new Fixture();
        ResidentEntity resident = ResidentEntity.builder().no(7).name("입주민").build();
        String title = "🚨 이중주차 알림";
        String body = "[a-b1-007] 구역에 이중주차가 확인되었습니다. 이동 주차 바랍니다.";

        when(fixture.residentRepository.findById(7)).thenReturn(Optional.of(resident));
        when(fixture.notificationRepository.findByResident_NoOrderByCreatedAtDesc(7)).thenReturn(List.of());
        when(fixture.deviceInfoRepository.findByResident_No(7)).thenReturn(List.of(
                DeviceInfoEntity.builder().fcmToken("token-1").resident(resident).build()
        ));

        fixture.service.sendPushToResident(7, title, body);

        verify(fixture.notificationRepository).save(any(AppNotificationEntity.class));
        verify(fixture.fcmService).sendPush("token-1", title, body);
    }

    private static class Fixture {
        private final ResidentRepository residentRepository = mock(ResidentRepository.class);
        private final ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        private final ResidentInquiryRepository residentInquiryRepository = mock(ResidentInquiryRepository.class);
        private final AppNotificationRepository notificationRepository = mock(AppNotificationRepository.class);
        private final DeviceInfoRepository deviceInfoRepository = mock(DeviceInfoRepository.class);
        private final AppSettingRepository settingRepository = mock(AppSettingRepository.class);
        private final WaitingListRepository waitingListRepository = mock(WaitingListRepository.class);
        private final RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        private final ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);
        private final FcmService fcmService = mock(FcmService.class);
        private final ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        private final ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        private final AppResidentFeatureService service = new AppResidentFeatureService(
                residentRepository,
                residentVehicleRepository,
                residentInquiryRepository,
                notificationRepository,
                deviceInfoRepository,
                settingRepository,
                waitingListRepository,
                registeredCarRepository,
                parkingZoneRepository,
                fcmService,
                managerNotificationService,
                parkingLotRepository
        );
    }
}
