package web.parking.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.parking.entity.ResidentVehicleEntity;

public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicleEntity, Integer> {

    Optional<ResidentVehicleEntity> findByNumber(String number);

    @Query(value = """
            select *
            from car
            where replace(c_number, ' ', '') = :compactNumber
            limit 1
            """, nativeQuery = true)
    Optional<ResidentVehicleEntity> findFirstByCompactNumber(@Param("compactNumber") String compactNumber);

    List<ResidentVehicleEntity> findByResident_No(Integer residentNo);

    List<ResidentVehicleEntity> findByResident_Apartment_No(Integer apartmentNo);

    List<ResidentVehicleEntity> findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(Integer apartmentNo);

    List<ResidentVehicleEntity> findByResident_Apartment_NoAndResident_DongAndResident_Ho(
            Integer apartmentNo,
            String dong,
            String ho
    );

    long deleteByNumberAndResident_No(String number, Integer residentNo);

    long countByResident_Apartment_No(Integer apartmentNo);

    long countByResident_No(Integer residentNo);
    
// 같은 아파트, 같은 동, 같은 호수에 등록된 차량의 총합을 세는 도구
    long countByResident_Apartment_NoAndResident_DongAndResident_Ho(Integer apartmentNo, String dong, String ho);

    boolean existsByNumberAndNoNot(String number, Integer vehicleNo);

    boolean existsByNumber(String number);
}
