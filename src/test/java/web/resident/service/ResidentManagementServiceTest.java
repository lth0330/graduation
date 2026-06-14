package web.resident.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.entity.AppNotificationEntity;
import app.entity.AppSettingEntity;
import app.entity.DeviceInfoEntity;
import app.repository.AppNotificationRepository;
import app.repository.AppSettingRepository;
import app.repository.DeviceInfoRepository;
import app.service.FcmService;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.repository.ApartmentRepository;
import web.common.type.ApprovalStatus;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.dto.ResidentContactNotificationRequestDto;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

class ResidentManagementServiceTest {

    @Test
    void findApprovedResidentsQueriesNewestResidentsFirst() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppNotificationRepository appNotificationRepository = mock(AppNotificationRepository.class);
        DeviceInfoRepository deviceInfoRepository = mock(DeviceInfoRepository.class);
        AppSettingRepository appSettingRepository = mock(AppSettingRepository.class);
        FcmService fcmService = mock(FcmService.class);
        when(residentRepository.findByApartment_NoAndApprovalStatusOrderByRegisteredAtDescNoDesc(
                1,
                ApprovalStatus.APPROVED
        )).thenReturn(List.of());

        ResidentManagementService service = new ResidentManagementService(
                residentRepository,
                residentVehicleRepository,
                apartmentRepository,
                passwordEncoder,
                appNotificationRepository,
                deviceInfoRepository,
                appSettingRepository,
                fcmService
        );

        service.findApprovedResidents(1);

        verify(residentRepository).findByApartment_NoAndApprovalStatusOrderByRegisteredAtDescNoDesc(
                1,
                ApprovalStatus.APPROVED
        );
    }

    @Test
    void sendContactNotificationStoresAppNotificationAndSendsPushToSameApartmentResident() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppNotificationRepository appNotificationRepository = mock(AppNotificationRepository.class);
        DeviceInfoRepository deviceInfoRepository = mock(DeviceInfoRepository.class);
        AppSettingRepository appSettingRepository = mock(AppSettingRepository.class);
        FcmService fcmService = mock(FcmService.class);
        ResidentManagementService service = new ResidentManagementService(
                residentRepository,
                residentVehicleRepository,
                apartmentRepository,
                passwordEncoder,
                appNotificationRepository,
                deviceInfoRepository,
                appSettingRepository,
                fcmService
        );
        ResidentEntity resident = ResidentEntity.builder()
                .no(7)
                .apartment(ApartmentEntity.builder().no(1).build())
                .name("홍길동")
                .build();
        DeviceInfoEntity device = DeviceInfoEntity.builder()
                .resident(resident)
                .deviceId("device_7")
                .fcmToken("fcm-token")
                .build();
        AppSettingEntity setting = AppSettingEntity.builder()
                .alertPush(true)
                .build();
        ResidentContactNotificationRequestDto requestDto = new ResidentContactNotificationRequestDto();
        requestDto.setTitle("통로 주차 확인 요청");
        requestDto.setMessage("a-b1-009 통로 주차 상태를 확인해주세요.");

        when(residentRepository.findById(7)).thenReturn(Optional.of(resident));
        when(deviceInfoRepository.findByResident_No(7)).thenReturn(List.of(device));
        when(appSettingRepository.findByDeviceId("device_7")).thenReturn(Optional.of(setting));

        Map<String, Object> result = service.sendContactNotification(
                Map.of("apartmentNo", 1),
                7,
                requestDto
        );

        assertThat(result).containsEntry("result", "ok");
        assertThat(result).containsEntry("resident_no", 7);
        verify(appNotificationRepository).save(any(AppNotificationEntity.class));
        verify(fcmService).sendPush(
                "fcm-token",
                "통로 주차 확인 요청",
                "a-b1-009 통로 주차 상태를 확인해주세요."
        );
    }

    @Test
    void sendContactNotificationRejectsResidentFromAnotherApartment() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppNotificationRepository appNotificationRepository = mock(AppNotificationRepository.class);
        DeviceInfoRepository deviceInfoRepository = mock(DeviceInfoRepository.class);
        AppSettingRepository appSettingRepository = mock(AppSettingRepository.class);
        FcmService fcmService = mock(FcmService.class);
        ResidentManagementService service = new ResidentManagementService(
                residentRepository,
                residentVehicleRepository,
                apartmentRepository,
                passwordEncoder,
                appNotificationRepository,
                deviceInfoRepository,
                appSettingRepository,
                fcmService
        );
        ResidentEntity resident = ResidentEntity.builder()
                .no(7)
                .apartment(ApartmentEntity.builder().no(2).build())
                .name("홍길동")
                .build();
        ResidentContactNotificationRequestDto requestDto = new ResidentContactNotificationRequestDto();
        requestDto.setTitle("통로 주차 확인 요청");
        requestDto.setMessage("a-b1-009 통로 주차 상태를 확인해주세요.");
        when(residentRepository.findById(7)).thenReturn(Optional.of(resident));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.sendContactNotification(Map.of("apartmentNo", 1), 7, requestDto)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(403);

        verify(appNotificationRepository, never()).save(any(AppNotificationEntity.class));
        verify(fcmService, never()).sendPush(any(), any(), any());
    }
}
