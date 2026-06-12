package web.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.notification.entity.ManagerNotificationEntity;

public interface ManagerNotificationRepository extends JpaRepository<ManagerNotificationEntity, Integer> {

    @Query("""
            select notification
            from ManagerNotificationEntity notification
            where notification.apartment.no = :apartmentNo
              and (notification.manager is null or notification.manager.no = :managerNo)
            order by notification.createdAt desc
            """)
    List<ManagerNotificationEntity> findVisibleToManager(
            @Param("apartmentNo") Integer apartmentNo,
            @Param("managerNo") Integer managerNo
    );

    long countByApartment_NoAndReadFalse(Integer apartmentNo);

    List<ManagerNotificationEntity> findByApartment_NoAndReferenceTypeAndReferenceIdAndReadFalse(
            Integer apartmentNo,
            String referenceType,
            Integer referenceId
    );

    List<ManagerNotificationEntity> findByApartment_NoAndTypeAndReferenceTypeAndReferenceIdAndReadFalse(
            Integer apartmentNo,
            String type,
            String referenceType,
            Integer referenceId
    );

    Optional<ManagerNotificationEntity> findFirstByApartment_NoAndTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterOrderByCreatedAtDesc(
            Integer apartmentNo,
            String type,
            String referenceType,
            Integer referenceId,
            LocalDateTime createdAtAfter
    );
}
