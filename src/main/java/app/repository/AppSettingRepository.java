package app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.AppSettingEntity;

public interface AppSettingRepository extends JpaRepository<AppSettingEntity, Integer> {

    Optional<AppSettingEntity> findByDeviceId(String deviceId);
}
