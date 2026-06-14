package web.parking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.inquiry.repository.ResidentInquiryRepository;
import web.parking.dto.VehicleOwnerDto;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.entity.ResidentEntity;
import web.resident.repository.ResidentRepository;

class VehicleManagementServiceTest {

    @Test
    void findVehiclesQueriesNewestVehiclesFirst() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentInquiryRepository residentInquiryRepository = mock(ResidentInquiryRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        when(residentVehicleRepository.findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(1))
                .thenReturn(List.of());

        VehicleManagementService service = new VehicleManagementService(
                residentVehicleRepository,
                residentRepository,
                residentInquiryRepository,
                registeredCarRepository
        );

        service.findVehicles(1);

        verify(residentVehicleRepository).findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(1);
    }

    @Test
    void findOwnerByCarNumberReturnsResidentVehicleOwnerInSameApartment() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentInquiryRepository residentInquiryRepository = mock(ResidentInquiryRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        VehicleManagementService service = new VehicleManagementService(
                residentVehicleRepository,
                residentRepository,
                residentInquiryRepository,
                registeredCarRepository
        );
        ResidentEntity resident = ResidentEntity.builder()
                .no(7)
                .apartment(ApartmentEntity.builder().no(1).build())
                .name("홍길동")
                .dong("101")
                .ho("1001")
                .phone("010-1234-5678")
                .build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .number("12가 3456")
                .resident(resident)
                .build();
        when(residentVehicleRepository.findFirstByCompactNumber("12가3456")).thenReturn(Optional.of(vehicle));

        VehicleOwnerDto result = service.findOwnerByCarNumber(Map.of("apartmentNo", 1), "12가3456");

        assertThat(result.getResidentNo()).isEqualTo(7);
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getPhone()).isEqualTo("010-1234-5678");
        assertThat(result.getCarNumber()).isEqualTo("12가 3456");
        assertThat(result.getVehicleType()).isEqualTo("resident");
    }

    @Test
    void findOwnerByCarNumberFallsBackToVisitorCarOwner() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentInquiryRepository residentInquiryRepository = mock(ResidentInquiryRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        VehicleManagementService service = new VehicleManagementService(
                residentVehicleRepository,
                residentRepository,
                residentInquiryRepository,
                registeredCarRepository
        );
        ResidentEntity resident = ResidentEntity.builder()
                .no(8)
                .apartment(ApartmentEntity.builder().no(1).build())
                .name("김철수")
                .dong("102")
                .ho("1102")
                .phone("010-1111-2222")
                .build();
        RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                .number("34나5678")
                .resident(resident)
                .build();
        when(residentVehicleRepository.findFirstByCompactNumber("34나5678")).thenReturn(Optional.empty());
        when(registeredCarRepository.findFirstByCompactNumber("34나5678")).thenReturn(Optional.of(visitorCar));

        VehicleOwnerDto result = service.findOwnerByCarNumber(Map.of("apartmentNo", 1), "34나5678");

        assertThat(result.getResidentNo()).isEqualTo(8);
        assertThat(result.getVehicleType()).isEqualTo("visitor");
    }

    @Test
    void findOwnerByCarNumberRejectsOwnerFromAnotherApartment() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentInquiryRepository residentInquiryRepository = mock(ResidentInquiryRepository.class);
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        VehicleManagementService service = new VehicleManagementService(
                residentVehicleRepository,
                residentRepository,
                residentInquiryRepository,
                registeredCarRepository
        );
        ResidentEntity resident = ResidentEntity.builder()
                .no(7)
                .apartment(ApartmentEntity.builder().no(2).build())
                .name("홍길동")
                .build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder()
                .number("12가3456")
                .resident(resident)
                .build();
        when(residentVehicleRepository.findFirstByCompactNumber("12가3456")).thenReturn(Optional.of(vehicle));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findOwnerByCarNumber(Map.of("apartmentNo", 1), "12가3456")
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(403);
    }
}
