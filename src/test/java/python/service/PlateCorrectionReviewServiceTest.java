package python.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.repository.RegisteredCarRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import python.entity.PlateCorrectionReviewEntity;
import python.repository.PlateCorrectionReviewRepository;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.notification.entity.ManagerNotificationEntity;
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;

class PlateCorrectionReviewServiceTest {

    @Test
    void recordNeedsReviewStoresPendingReviewAndCreatesManagerNotification() {
        ServiceFixture fixture = new ServiceFixture();
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder().areaNumber("A1").build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("A1")
                .plate("UNKNOWN")
                .status("PARKED")
                .build();

        when(fixture.reviewRepository.save(any(PlateCorrectionReviewEntity.class)))
                .thenAnswer(invocation -> {
                    PlateCorrectionReviewEntity review = invocation.getArgument(0);
                    review.setNo(30);
                    return review;
                });
        when(fixture.managerNotificationService.createApartmentNotification(
                eq(apartment),
                eq("plate_review_required"),
                eq("번호판 확인 필요"),
                contains("OCR 12가1235"),
                eq("plate_correction_review"),
                eq(30)
        )).thenReturn(ManagerNotificationEntity.builder().no(40).build());

        PlateCorrectionReviewEntity result = fixture.service.recordNeedsReview(
                apartment,
                history,
                "A1",
                "12가1235",
                null,
                List.of("12가1234", "12가1236"),
                1
        );

        assertThat(result.getNo()).isEqualTo(30);
        assertThat(result.getStatus()).isEqualTo("NEEDS_REVIEW");
        assertThat(result.getCandidateList()).isEqualTo("12가1234,12가1236");
        verify(fixture.managerNotificationService).createApartmentNotification(
                eq(apartment),
                eq("plate_review_required"),
                eq("번호판 확인 필요"),
                contains("A1 구역"),
                eq("plate_correction_review"),
                eq(30)
        );
    }

    @Test
    void confirmReviewUpdatesHistoryZoneLinkedZoneAndMarksNotificationRead() {
        ServiceFixture fixture = new ServiceFixture();
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .areaNumber("A1")
                .currentCarNumber("UNKNOWN")
                .build();
        ParkingZoneEntity linkedZone = ParkingZoneEntity.builder()
                .areaNumber("A2")
                .currentCarNumber("UNKNOWN")
                .build();
        ResidentVehicleEntity vehicle = ResidentVehicleEntity.builder().number("12가1234").build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("A1")
                .linkedZone("A2")
                .plate("UNKNOWN")
                .status("PARKED")
                .build();
        PlateCorrectionReviewEntity review = PlateCorrectionReviewEntity.builder()
                .no(30)
                .apartment(apartment)
                .parkingHistory(history)
                .zone("A1")
                .ocrPlate("12가1235")
                .candidateList("12가1234,12가1236")
                .distance(1)
                .status("NEEDS_REVIEW")
                .build();

        when(fixture.apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(fixture.reviewRepository.findById(30)).thenReturn(Optional.of(review));
        when(fixture.residentVehicleRepository.findByNumber("12가1234")).thenReturn(Optional.of(vehicle));
        when(fixture.registeredCarRepository.findFirstByNumberAndParkedAtIsNull("12가1234")).thenReturn(Optional.empty());
        when(fixture.parkingZoneRepository.findByAreaNumber("A2")).thenReturn(Optional.of(linkedZone));

        Map<String, Object> result = fixture.service.confirmReview(7, 30, "12가1234");

        assertThat(result.get("plate")).isEqualTo("12가1234");
        assertThat(history.getPlate()).isEqualTo("12가1234");
        assertThat(history.getResidentVehicle()).isEqualTo(vehicle);
        assertThat(zone.getCurrentCarNumber()).isEqualTo("12가1234");
        assertThat(linkedZone.getCurrentCarNumber()).isEqualTo("12가1234");
        assertThat(review.getStatus()).isEqualTo("CONFIRMED");
        assertThat(review.getSelectedPlate()).isEqualTo("12가1234");
        verify(fixture.managerNotificationService).markReferenceAsRead(
                apartment,
                "plate_correction_review",
                30
        );
    }

    @Test
    void confirmReviewAfterExitUpdatesHistoryOnlyAndKeepsCurrentZoneCar() {
        ServiceFixture fixture = new ServiceFixture();
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        // 확정 전에 출차했고, 지금 그 칸에는 다른 차가 주차 중이다.
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .areaNumber("A1")
                .currentCarNumber("34나5678")
                .build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("A1")
                .plate("UNKNOWN")
                .status("EXITED")
                .exitTime(java.time.LocalDateTime.now())
                .build();
        PlateCorrectionReviewEntity review = PlateCorrectionReviewEntity.builder()
                .no(30)
                .apartment(apartment)
                .parkingHistory(history)
                .zone("A1")
                .status("NEEDS_REVIEW")
                .build();

        when(fixture.apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(fixture.reviewRepository.findById(30)).thenReturn(Optional.of(review));
        when(fixture.residentVehicleRepository.findByNumber("12가1234")).thenReturn(Optional.empty());
        when(fixture.residentVehicleRepository.findFirstByCompactNumber("12가1234")).thenReturn(Optional.empty());
        when(fixture.registeredCarRepository.findFirstByNumberAndParkedAtIsNull("12가1234")).thenReturn(Optional.empty());
        when(fixture.registeredCarRepository.findFirstByCompactNumberAndParkedAtIsNull("12가1234")).thenReturn(Optional.empty());

        fixture.service.confirmReview(7, 30, "12가1234");

        assertThat(history.getPlate()).isEqualTo("12가1234");
        assertThat(zone.getCurrentCarNumber()).isEqualTo("34나5678");
        assertThat(review.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirmReviewRejectsAlreadyResolvedReview() {
        ServiceFixture fixture = new ServiceFixture();
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        PlateCorrectionReviewEntity review = PlateCorrectionReviewEntity.builder()
                .no(30)
                .apartment(apartment)
                .parkingHistory(ParkingHistoryEntity.builder().id(11).status("PARKED").build())
                .status("AUTO_RESOLVED")
                .build();

        when(fixture.apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(fixture.reviewRepository.findById(30)).thenReturn(Optional.of(review));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fixture.service.confirmReview(7, 30, "12가1234"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("이미 처리된");
    }

    @Test
    void resolveByPlateClosesPendingReviewAndMarksNotificationRead() {
        ServiceFixture fixture = new ServiceFixture();
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder().id(11).status("PARKED").build();
        PlateCorrectionReviewEntity review = PlateCorrectionReviewEntity.builder()
                .no(30)
                .apartment(apartment)
                .parkingHistory(history)
                .status("NEEDS_REVIEW")
                .build();
        when(fixture.reviewRepository.findByParkingHistory_IdAndStatus(11, "NEEDS_REVIEW")).thenReturn(List.of(review));

        fixture.service.resolveByPlate(history, "12가 1234");

        assertThat(review.getStatus()).isEqualTo("AUTO_RESOLVED");
        assertThat(review.getSelectedPlate()).isEqualTo("12가1234");
        assertThat(review.getResolvedAt()).isNotNull();
        verify(fixture.managerNotificationService).markReferenceAsRead(apartment, "plate_correction_review", 30);
    }

    @Test
    void resolveByPlateIgnoresUnknownPlate() {
        ServiceFixture fixture = new ServiceFixture();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder().id(11).status("PARKED").build();

        fixture.service.resolveByPlate(history, "UNKNOWN");

        org.mockito.Mockito.verifyNoInteractions(fixture.reviewRepository);
    }

    @Test
    void cancelPendingReviewMarksReviewCancelled() {
        ServiceFixture fixture = new ServiceFixture();
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder().id(11).status("EXITED").build();
        PlateCorrectionReviewEntity review = PlateCorrectionReviewEntity.builder()
                .no(30)
                .apartment(apartment)
                .parkingHistory(history)
                .status("NEEDS_REVIEW")
                .build();
        when(fixture.reviewRepository.findByParkingHistory_IdAndStatus(11, "NEEDS_REVIEW")).thenReturn(List.of(review));

        fixture.service.cancelPendingReview(history);

        assertThat(review.getStatus()).isEqualTo("CANCELLED");
        assertThat(review.getSelectedPlate()).isNull();
        verify(fixture.managerNotificationService).markReferenceAsRead(apartment, "plate_correction_review", 30);
    }

    private static class ServiceFixture {
        private final PlateCorrectionReviewRepository reviewRepository = mock(PlateCorrectionReviewRepository.class);
        private final ManagerNotificationService managerNotificationService = mock(ManagerNotificationService.class);
        private final ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        private final ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        private final RegisteredCarRepository registeredCarRepository = mock(RegisteredCarRepository.class);
        private final ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);

        private final PlateCorrectionReviewService service = new PlateCorrectionReviewService(
                reviewRepository,
                managerNotificationService,
                apartmentManagerRepository,
                residentVehicleRepository,
                registeredCarRepository,
                parkingZoneRepository
        );
    }
}
