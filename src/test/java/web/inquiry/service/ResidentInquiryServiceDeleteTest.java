package web.inquiry.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.repository.AppNotificationRepository;
import app.repository.AppSettingRepository;
import app.repository.DeviceInfoRepository;
import app.service.FcmService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.inquiry.entity.ResidentInquiryEntity;
import web.inquiry.repository.ResidentInquiryRepository;
import web.notification.service.ManagerNotificationService;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

class ResidentInquiryServiceDeleteTest {

    private ResidentInquiryRepository residentInquiryRepository;
    private ApartmentManagerRepository apartmentManagerRepository;
    private ManagerNotificationService managerNotificationService;
    private ResidentInquiryService service;

    private final ApartmentEntity myApartment = ApartmentEntity.builder().no(1).name("내 아파트").build();
    private final ApartmentEntity otherApartment = ApartmentEntity.builder().no(2).name("다른 아파트").build();

    @BeforeEach
    void setUp() {
        residentInquiryRepository = mock(ResidentInquiryRepository.class);
        apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        managerNotificationService = mock(ManagerNotificationService.class);
        service = new ResidentInquiryService(
                residentInquiryRepository,
                mock(ResidentRepository.class),
                mock(ResidentVehicleRepository.class),
                apartmentManagerRepository,
                managerNotificationService,
                mock(AppNotificationRepository.class),
                mock(DeviceInfoRepository.class),
                mock(FcmService.class),
                mock(AppSettingRepository.class)
        );
        when(apartmentManagerRepository.findById(10)).thenReturn(Optional.of(
                ApartmentManagerEntity.builder().no(10).apartment(myApartment).build()
        ));
    }

    private ResidentInquiryEntity inquiryOf(ApartmentEntity apartment) {
        ResidentEntity resident = ResidentEntity.builder().no(5).apartment(apartment).build();
        return ResidentInquiryEntity.builder().no(100).resident(resident).title("t").content("c").status("pending").build();
    }

    @Test
    void deletesOwnApartmentInquiryAndMarksNotificationRead() {
        ResidentInquiryEntity inquiry = inquiryOf(myApartment);
        when(residentInquiryRepository.findById(100)).thenReturn(Optional.of(inquiry));

        service.delete(Map.of("userNo", 10), 100);

        verify(managerNotificationService).markReferenceAsRead(myApartment, "resident_inquiry", 100);
        verify(residentInquiryRepository).delete(inquiry);
    }

    @Test
    void rejectsOtherApartmentInquiry() {
        when(residentInquiryRepository.findById(100)).thenReturn(Optional.of(inquiryOf(otherApartment)));

        assertThatThrownBy(() -> service.delete(Map.of("userNo", 10), 100))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("다른 아파트");
        verify(residentInquiryRepository, never()).delete(any());
    }
}
