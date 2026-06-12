package app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import app.entity.RegisteredCarEntity;

public interface RegisteredCarRepository extends JpaRepository<RegisteredCarEntity, Integer> {

    // 👇 [해결 1] 지금 오류가 난 바로 그 메서드입니다! 추가해주세요.
    Optional<RegisteredCarEntity> findFirstByNumber(String number);

    @Query(value = """
            select *
            from registered_cars
            where replace(c_number, ' ', '') = :compactNumber
            limit 1
            """, nativeQuery = true)
    Optional<RegisteredCarEntity> findFirstByCompactNumber(@Param("compactNumber") String compactNumber);

    List<RegisteredCarEntity> findByResident_No(Integer residentNo);

    List<RegisteredCarEntity> findByResident_Apartment_No(Integer apartmentNo);

    List<RegisteredCarEntity> findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(Integer apartmentNo);

    Optional<RegisteredCarEntity> findFirstByNumberAndParkedAtIsNull(String number);

    @Query(value = """
            select *
            from registered_cars
            where replace(c_number, ' ', '') = :compactNumber
              and park_time is null
            limit 1
            """, nativeQuery = true)
    Optional<RegisteredCarEntity> findFirstByCompactNumberAndParkedAtIsNull(@Param("compactNumber") String compactNumber);

    boolean existsByNumber(String number);

    long deleteByNumberAndResident_No(String number, Integer residentNo);

    // 같은 아파트, 같은 동, 같은 호수에 등록된 차량의 총합을 세는 도구
    long countByResident_Apartment_NoAndResident_DongAndResident_Ho(Integer apartmentNo, String dong, String ho);

    long countByResident_No(Integer residentNo);
}
