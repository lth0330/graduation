package web.parking.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import web.parking.entity.ParkingHistoryEntity;

public interface ParkingHistoryRepository extends JpaRepository<ParkingHistoryEntity, Integer> {

    List<ParkingHistoryEntity> findByParkingZone_NoOrderByEntryTimeDesc(Integer parkingZoneNo);

    List<ParkingHistoryEntity> findByPlateOrderByEntryTimeDesc(String plate);
}
