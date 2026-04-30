package web.aptManager.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.aptManager.entity.ResidentVehicleEntity;

public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicleEntity, Integer> {

    Optional<ResidentVehicleEntity> findByNumber(String number);

    List<ResidentVehicleEntity> findByResident_No(Integer residentNo);

    boolean existsByNumber(String number);
}
