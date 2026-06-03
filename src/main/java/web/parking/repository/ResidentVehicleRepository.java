package web.parking.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.parking.entity.ResidentVehicleEntity;

public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicleEntity, Integer> {

    Optional<ResidentVehicleEntity> findByNumber(String number);

    List<ResidentVehicleEntity> findByResident_No(Integer residentNo);

    List<ResidentVehicleEntity> findByResident_Apartment_No(Integer apartmentNo);

    List<ResidentVehicleEntity> findByResident_Apartment_NoAndResident_DongAndResident_Ho(
            Integer apartmentNo,
            String dong,
            String ho
    );

    long deleteByNumberAndResident_No(String number, Integer residentNo);

    long countByResident_Apartment_No(Integer apartmentNo);

    long countByResident_No(Integer residentNo);

    long countByResident_Apartment_NoAndResident_DongAndResident_Ho(Integer apartmentNo, String dong, String ho);

    boolean existsByNumberAndNoNot(String number, Integer vehicleNo);

    boolean existsByNumber(String number);
}
