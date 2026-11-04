package python.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import python.entity.PlateCorrectionReviewEntity;
import python.repository.PlateCorrectionReviewRepository;
import web.aptManager.entity.ApartmentEntity;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingHistoryRepository;

class UnknownPlateReviewSchedulerTest {

    private final ParkingHistoryRepository historyRepository = mock(ParkingHistoryRepository.class);
    private final PlateCorrectionReviewRepository reviewRepository = mock(PlateCorrectionReviewRepository.class);
    private final PlateCorrectionReviewService reviewService = mock(PlateCorrectionReviewService.class);
    private final UnknownPlateReviewScheduler scheduler =
            new UnknownPlateReviewScheduler(historyRepository, reviewRepository, reviewService);

    private final ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("성결아파트").build();
    private final ParkingZoneEntity zone = ParkingZoneEntity.builder()
            .areaNumber("a-b1-008")
            .parkingLot(ParkingLotEntity.builder().no(1).apartment(apartment).build())
            .build();

    private ParkingHistoryEntity unknownHistory(int id) {
        return ParkingHistoryEntity.builder()
                .id(id)
                .parkingZone(zone)
                .zoneSnapshot("a-b1-008")
                .plate("UNKNOWN")
                .status("PARKED")
                .build();
    }

    @Test
    void unknownPlateParkedLongerThanWaitBecomesReviewWithoutCandidates() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 4, 16, 30, 0);
        ParkingHistoryEntity history = unknownHistory(9071);
        when(historyRepository.findByPlateAndStatusAndExitTimeIsNullAndEntryTimeBefore(
                "UNKNOWN", "PARKED", now.minusSeconds(UnknownPlateReviewScheduler.WAIT_SECONDS)))
                .thenReturn(List.of(history));
        when(reviewRepository.existsByParkingHistory_Id(9071)).thenReturn(false);
        when(reviewService.recordNeedsReview(any(), any(), any(), any(), any(), anyList(), any()))
                .thenReturn(new PlateCorrectionReviewEntity());

        int created = scheduler.createReviewsForUnknownPlates(now);

        assertThat(created).isEqualTo(1);
        verify(reviewService).recordNeedsReview(
                eq(apartment), eq(history), eq("a-b1-008"), eq("UNKNOWN"), isNull(), eq(List.of()), isNull());
    }

    @Test
    void historyThatAlreadyHadReviewIsNotAddedAgain() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 4, 16, 30, 0);
        when(historyRepository.findByPlateAndStatusAndExitTimeIsNullAndEntryTimeBefore(any(), any(), any()))
                .thenReturn(List.of(unknownHistory(9071)));
        when(reviewRepository.existsByParkingHistory_Id(9071)).thenReturn(true);

        int created = scheduler.createReviewsForUnknownPlates(now);

        assertThat(created).isZero();
        verify(reviewService, never()).recordNeedsReview(any(), any(), any(), any(), any(), anyList(), any());
    }

    @Test
    void historyWithoutApartmentIsSkipped() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 4, 16, 30, 0);
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(9072).plate("UNKNOWN").status("PARKED").zoneSnapshot("a-b1-001").build();
        when(historyRepository.findByPlateAndStatusAndExitTimeIsNullAndEntryTimeBefore(any(), any(), any()))
                .thenReturn(List.of(history));
        when(reviewRepository.existsByParkingHistory_Id(9072)).thenReturn(false);

        assertThat(scheduler.createReviewsForUnknownPlates(now)).isZero();
        verify(reviewService, never()).recordNeedsReview(any(), any(), any(), any(), any(), anyList(), any());
    }
}
