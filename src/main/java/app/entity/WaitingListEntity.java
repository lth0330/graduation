package app.entity;

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
import web.resident.entity.ResidentEntity;

@Entity
@Table(name = "waiting_list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wait_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_no", nullable = false)
    private ResidentEntity resident;

    @Column(name = "target_slot_id", length = 10)
    private String targetSlotId;

    @Column(name = "is_notified")
    private Boolean notified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (notified == null) {
            notified = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
