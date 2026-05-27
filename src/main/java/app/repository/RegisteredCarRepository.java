package app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.RegisteredCarEntity;

public interface RegisteredCarRepository extends JpaRepository<RegisteredCarEntity, Integer> {

    List<RegisteredCarEntity> findByResident_No(Integer residentNo);
    Optional<RegisteredCarEntity> findFirstByNumberAndParkedAtIsNull(String number);

    // 👇 2번 버전에서 가져온 유용한 코드 (이 줄을 추가해 주세요!)
    boolean existsByNumber(String number); 
    long deleteByNumberAndResident_No(String number, Integer residentNo);
}
