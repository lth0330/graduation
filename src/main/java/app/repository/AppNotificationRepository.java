package app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.AppNotificationEntity;

public interface AppNotificationRepository extends JpaRepository<AppNotificationEntity, Integer> {

    List<AppNotificationEntity> findByResident_NoOrderByCreatedAtDesc(Integer residentNo);
}
