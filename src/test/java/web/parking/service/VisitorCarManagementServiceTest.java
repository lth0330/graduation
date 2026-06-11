package web.parking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import python.entity.GateEntryLogEntity;
import python.repository.GateEntryLogRepository;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.parking.dto.VisitorCarManagementDto;
import web.resident.entity.ResidentEntity;

class VisitorCarManagementServiceTest {

    @Test
    void updateExpiresAtChangesVisitorCarExpirationTime() {
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        VisitorCarManagementService service = new VisitorCarManagementService(registeredCarRepository, gateEntryLogRepository);
        RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                .no(10)
                .number("123가4567")
                .expiresAt(LocalDateTime.of(2026, 6, 9, 12, 0))
                .resident(ResidentEntity.builder()
                        .apartment(ApartmentEntity.builder().no(1).build())
                        .build())
                .build();
        LocalDateTime newExpiresAt = LocalDateTime.of(2026, 6, 10, 18, 30);
        when(registeredCarRepository.findById(10)).thenReturn(Optional.of(visitorCar));

        VisitorCarManagementDto result = service.updateExpiresAt(10, 1, newExpiresAt);

        assertThat(visitorCar.getExpiresAt()).isEqualTo(newExpiresAt);
        assertThat(result.getExpiresAt()).isEqualTo(newExpiresAt);
    }

    @Test
    void deleteRemovesVisitorCar() {
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        VisitorCarManagementService service = new VisitorCarManagementService(registeredCarRepository, gateEntryLogRepository);
        RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                .no(10)
                .number("123가4567")
                .resident(ResidentEntity.builder()
                        .apartment(ApartmentEntity.builder().no(1).build())
                        .build())
                .build();
        when(registeredCarRepository.findById(10)).thenReturn(Optional.of(visitorCar));

        service.delete(10, 1);

        verify(registeredCarRepository).delete(visitorCar);
    }

    @Test
    void updateExpiresAtRejectsVisitorCarFromAnotherApartment() {
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        VisitorCarManagementService service = new VisitorCarManagementService(registeredCarRepository, gateEntryLogRepository);
        RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                .no(10)
                .number("123가4567")
                .resident(ResidentEntity.builder()
                        .apartment(ApartmentEntity.builder().no(2).build())
                        .build())
                .build();
        when(registeredCarRepository.findById(10)).thenReturn(Optional.of(visitorCar));

        assertThatThrownBy(() -> service.updateExpiresAt(10, 1, LocalDateTime.of(2026, 6, 10, 18, 30)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("접근할 수 없는 방문 차량입니다.");
    }

    @Test
    void findVisitorCarsUsesGateOpenLogAsGateEntryTimeBeforeParking() {
        RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        GateEntryLogRepository gateEntryLogRepository = mock(GateEntryLogRepository.class);
        VisitorCarManagementService service = new VisitorCarManagementService(registeredCarRepository, gateEntryLogRepository);
        LocalDateTime gateTime = LocalDateTime.of(2026, 6, 11, 9, 15);
        RegisteredCarEntity visitorCar = RegisteredCarEntity.builder()
                .no(10)
                .number("123가4567")
                .parkedAt(null)
                .resident(ResidentEntity.builder()
                        .apartment(ApartmentEntity.builder().no(1).build())
                        .build())
                .build();

        when(registeredCarRepository.findByResident_Apartment_No(1)).thenReturn(List.of(visitorCar));
        when(gateEntryLogRepository.findFirstByPlateAndGateOpenTrueOrderByGateTimeDesc("123가4567"))
                .thenReturn(Optional.of(GateEntryLogEntity.builder()
                        .plate("123가4567")
                        .gateOpen(true)
                        .gateTime(gateTime)
                        .build()));

        List<VisitorCarManagementDto> result = service.findVisitorCars(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGateEnteredAt()).isEqualTo(gateTime);
        assertThat(result.get(0).getParkedAt()).isNull();
    }
}
