package python.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import python.entity.GateEntryLogEntity;

public interface GateEntryLogRepository extends JpaRepository<GateEntryLogEntity, Integer> {
}
