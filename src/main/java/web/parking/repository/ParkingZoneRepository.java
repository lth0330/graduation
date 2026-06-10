package web.parking.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.parking.entity.ParkingZoneEntity;

public interface ParkingZoneRepository extends JpaRepository<ParkingZoneEntity, Integer> {

    List<ParkingZoneEntity> findAllByOrderByNoAsc();

    Optional<ParkingZoneEntity> findByAreaNumber(String areaNumber);

    List<ParkingZoneEntity> findByParkingLot_No(Integer parkingLotNo);

    boolean existsByParkingLot_NoAndZoneTypeAndStatus(Integer parkingLotNo, String zoneType, String status);

    long countByParkingLot_NoAndStatus(Integer parkingLotNo, String status);

    long countByParkingLot_NoAndZoneTypeAndStatus(Integer parkingLotNo, String zoneType, String status);

    void deleteByParkingLot_No(Integer parkingLotNo);

    boolean existsByParkingLot_NoAndLayoutRowAndLayoutColumn(Integer parkingLotNo, Integer layoutRow, Integer layoutColumn);

    boolean existsByParkingLot_NoAndLayoutRowAndLayoutColumnAndNoNot(
            Integer parkingLotNo,
            Integer layoutRow,
            Integer layoutColumn,
            Integer zoneNo
    );
}
