package web.parking.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import web.parking.entity.ParkingLotEntity;

public interface ParkingLotRepository extends JpaRepository<ParkingLotEntity, Integer> {

    List<ParkingLotEntity> findByApartment_No(Integer apartmentNo);
}
