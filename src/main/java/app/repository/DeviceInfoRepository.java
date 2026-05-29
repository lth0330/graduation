package app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.DeviceInfoEntity;
import org.springframework.transaction.annotation.Transactional; // 💡 추가

public interface DeviceInfoRepository extends JpaRepository<DeviceInfoEntity, String> {

    List<DeviceInfoEntity> findByResident_No(Integer residentNo);
    // 💡 추가: 유효하지 않은 FCM 토큰을 가진 기기 정보를 삭제합니다.
    @Transactional
    void deleteByFcmToken(String fcmToken);
}
