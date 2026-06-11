package python.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import python.entity.GateEntryLogEntity;

public interface GateEntryLogRepository extends JpaRepository<GateEntryLogEntity, Integer> {

    Optional<GateEntryLogEntity> findFirstByPlateAndGateOpenTrueOrderByGateTimeDesc(String plate);
}
