package app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.RegisteredCarEntity;

public interface RegisteredCarRepository extends JpaRepository<RegisteredCarEntity, Integer> {

    List<RegisteredCarEntity> findByResident_No(Integer residentNo);

    List<RegisteredCarEntity> findByResident_Apartment_No(Integer apartmentNo);

    Optional<RegisteredCarEntity> findFirstByNumberAndParkedAtIsNull(String number);

    boolean existsByNumber(String number);

    long deleteByNumberAndResident_No(String number, Integer residentNo);

    long countByResident_No(Integer residentNo);
}
