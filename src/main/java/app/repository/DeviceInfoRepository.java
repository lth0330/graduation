package app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.DeviceInfoEntity;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfoEntity, String> {

    List<DeviceInfoEntity> findByResident_No(Integer residentNo);
}
