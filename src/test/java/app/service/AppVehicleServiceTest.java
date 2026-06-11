package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.dto.AppCarSaveRequestDto;
import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import web.aptManager.entity.ApartmentEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

class AppVehicleServiceTest {

    @Test
    void createVisitorCarDoesNotStartExpirationBeforeGateEntry() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        AppVehicleService service = new AppVehicleService(
                residentRepository,
                residentVehicleRepository,
                registeredCarRepository
        );
        ResidentEntity resident = ResidentEntity.builder()
                .no(7)
                .dong("101")
                .ho("1001")
                .visitorCarLimit(2)
                .apartment(ApartmentEntity.builder().no(1).build())
                .build();
        AppCarSaveRequestDto requestDto = new AppCarSaveRequestDto();
        requestDto.setNumber("12가1234");
        requestDto.setCarType("방문객");

        when(residentRepository.findById(7)).thenReturn(Optional.of(resident));
        when(residentVehicleRepository.existsByNumber("12가1234")).thenReturn(false);
        when(registeredCarRepository.existsByNumber("12가1234")).thenReturn(false);
        when(registeredCarRepository.countByResident_Apartment_NoAndResident_DongAndResident_Ho(1, "101", "1001"))
                .thenReturn(0L);

        service.create(7, requestDto);

        ArgumentCaptor<RegisteredCarEntity> visitorCarCaptor = ArgumentCaptor.forClass(RegisteredCarEntity.class);
        verify(registeredCarRepository).save(visitorCarCaptor.capture());
        assertThat(visitorCarCaptor.getValue().getParkedAt()).isNull();
        assertThat(visitorCarCaptor.getValue().getExpiresAt()).isNull();
    }
}
