package web.resident.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.common.type.ApprovalStatus;
import web.resident.entity.ResidentEntity;

public interface ResidentRepository extends JpaRepository<ResidentEntity, Integer> {

    Optional<ResidentEntity> findByLoginId(String loginId);

    List<ResidentEntity> findByApartment_No(Integer apartmentNo);

    List<ResidentEntity> findByApartment_NoOrderByRegisteredAtDescNoDesc(Integer apartmentNo);

    List<ResidentEntity> findByApartment_NoAndApprovalStatus(Integer apartmentNo, ApprovalStatus approvalStatus);

    List<ResidentEntity> findByApartment_NoAndApprovalStatusOrderByRegisteredAtDescNoDesc(
            Integer apartmentNo,
            ApprovalStatus approvalStatus
    );

    List<ResidentEntity> findByApartment_NoAndDongAndHo(Integer apartmentNo, String dong, String ho);

    long countByApartment_NoAndApprovalStatus(Integer apartmentNo, ApprovalStatus approvalStatus);

    List<ResidentEntity> findByDongAndHo(String dong, String ho);

    List<ResidentEntity> findByDongAndHoAndApartment_Password(String dong, String ho, String apartmentPassword);

    boolean existsByLoginId(String loginId);
}
