package web.notification.entity;

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
import web.aptManager.entity.ApartmentManagerEntity;

@Entity
@Table(name = "manager_notification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_no")
    private ApartmentManagerEntity manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_no", nullable = false)
    private ApartmentEntity apartment;

    @Column(name = "notification_type", length = 30, nullable = false)
    private String type;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "message", length = 255, nullable = false)
    private String message;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "is_read", nullable = false)
    private Boolean read;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (read == null) {
            read = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
