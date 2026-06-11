package python.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;
import web.parking.entity.ParkingHistoryEntity;

@Entity
@Table(name = "plate_correction_review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateCorrectionReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_no", nullable = false)
    private ApartmentEntity apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private ParkingHistoryEntity parkingHistory;

    @Column(name = "zone_name", length = 50, nullable = false)
    private String zone;

    @Column(name = "ocr_plate", length = 50)
    private String ocrPlate;

    @Column(name = "matched_plate", length = 50)
    private String matchedPlate;

    @Column(name = "selected_plate", length = 50)
    private String selectedPlate;

    @Column(name = "candidate_list", length = 500)
    private String candidateList;

    @Column(name = "match_distance")
    private Integer distance;

    @Column(name = "review_status", length = 30, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) {
            status = "NEEDS_REVIEW";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
