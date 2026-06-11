package python.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import python.entity.PlateCorrectionReviewEntity;

public interface PlateCorrectionReviewRepository extends JpaRepository<PlateCorrectionReviewEntity, Integer> {

    List<PlateCorrectionReviewEntity> findByApartment_NoAndStatusOrderByCreatedAtDesc(Integer apartmentNo, String status);

    Optional<PlateCorrectionReviewEntity> findFirstByParkingHistory_IdAndStatusOrderByCreatedAtDesc(
            Integer historyId,
            String status
    );
}
