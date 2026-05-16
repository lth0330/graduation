package web.aptManager.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.aptManager.entity.ApartmentManagerEntity;
import web.common.type.ApprovalStatus;

public interface ApartmentManagerRepository extends JpaRepository<ApartmentManagerEntity, Integer> {

    Optional<ApartmentManagerEntity> findByLoginId(String loginId);

    Optional<ApartmentManagerEntity> findByEmail(String email);

    List<ApartmentManagerEntity> findByApartment_No(Integer apartmentNo);

    long countByApprovalStatus(ApprovalStatus approvalStatus);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);
}
