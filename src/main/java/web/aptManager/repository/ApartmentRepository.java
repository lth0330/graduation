package web.aptManager.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.aptManager.entity.ApartmentEntity;

public interface ApartmentRepository extends JpaRepository<ApartmentEntity, Integer> {

    Optional<ApartmentEntity> findByName(String name);

    Optional<ApartmentEntity> findByPassword(String password);

    boolean existsByName(String name);

    boolean existsByPassword(String password);
}
