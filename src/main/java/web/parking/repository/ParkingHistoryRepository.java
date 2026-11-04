package web.parking.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.parking.entity.ParkingHistoryEntity;

public interface ParkingHistoryRepository extends JpaRepository<ParkingHistoryEntity, Integer> {

    List<ParkingHistoryEntity> findByParkingZone_NoOrderByEntryTimeDesc(Integer parkingZoneNo);

    List<ParkingHistoryEntity> findByPlateOrderByEntryTimeDesc(String plate);

    Optional<ParkingHistoryEntity> findFirstByParkingZone_NoAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(
            Integer parkingZoneNo,
            String status
    );

    List<ParkingHistoryEntity> findByPlateAndStatusAndExitTimeIsNullOrderByEntryTimeDesc(String plate, String status);

    // 번호 미확인(UNKNOWN)으로 일정 시간 넘게 주차 중인 기록 → 관리자 직접 입력 대상
    List<ParkingHistoryEntity> findByPlateAndStatusAndExitTimeIsNullAndEntryTimeBefore(
            String plate,
            String status,
            java.time.LocalDateTime entryTimeBefore
    );
}
