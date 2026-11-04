package python.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import python.repository.PlateCorrectionReviewRepository;
import web.aptManager.entity.ApartmentEntity;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingHistoryRepository;

/**
 * 번호를 끝내 읽지 못한(UNKNOWN) 주차 차량을 번호판 보정 목록에 올린다.
 * 보정 목록은 원래 "후보가 2개 이상"일 때만 생겨서, 번호를 아예 못 읽은 차는 관리자가 고칠 방법이 없었다.
 * 입차 직후에는 OCR·역추적으로 1~3초 안에 번호가 채워지는 경우가 많아 WAIT_SECONDS 동안 기다린 뒤 올린다.
 * 이후 번호가 자동으로 채워지거나 출차하면 기존 로직(resolveByPlate / cancelPendingReview)이 항목을 내린다.
 */
@Component
@RequiredArgsConstructor
public class UnknownPlateReviewScheduler {

    static final long WAIT_SECONDS = 20;
    private static final String UNKNOWN_PLATE = "UNKNOWN";
    private static final String HISTORY_PARKED = "PARKED";

    private final ParkingHistoryRepository parkingHistoryRepository;
    private final PlateCorrectionReviewRepository reviewRepository;
    private final PlateCorrectionReviewService reviewService;

    @Scheduled(fixedDelay = 10_000, initialDelay = 10_000)
    @Transactional
    public void run() {
        createReviewsForUnknownPlates(LocalDateTime.now());
    }

    @Transactional
    public int createReviewsForUnknownPlates(LocalDateTime now) {
        List<ParkingHistoryEntity> histories = parkingHistoryRepository
                .findByPlateAndStatusAndExitTimeIsNullAndEntryTimeBefore(
                        UNKNOWN_PLATE,
                        HISTORY_PARKED,
                        now.minusSeconds(WAIT_SECONDS)
                );

        int created = 0;
        for (ParkingHistoryEntity history : histories) {
            // 이미 한 번 올린 기록은 다시 올리지 않는다 (관리자가 확정·자동 처리한 뒤 알림이 반복되지 않게)
            if (history.getId() == null || reviewRepository.existsByParkingHistory_Id(history.getId())) {
                continue;
            }
            ApartmentEntity apartment = resolveApartment(history.getParkingZone());
            if (apartment == null) {
                continue;
            }
            String zoneName = history.getParkingZone() != null
                    ? history.getParkingZone().getAreaNumber()
                    : history.getZoneSnapshot();
            if (reviewService.recordNeedsReview(apartment, history, zoneName, UNKNOWN_PLATE, null, List.of(), null) != null) {
                created++;
            }
        }
        return created;
    }

    private ApartmentEntity resolveApartment(ParkingZoneEntity zone) {
        ParkingLotEntity parkingLot = zone != null ? zone.getParkingLot() : null;
        return parkingLot != null ? parkingLot.getApartment() : null;
    }
}
