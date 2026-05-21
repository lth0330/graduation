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
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_no", nullable = false)
    private ResidentEntity resident;

    @Column(name = "noti_type", length = 20)
    private String type;

    @Column(name = "noti_title", length = 100, nullable = false)
    private String title;

    @Column(name = "noti_message", nullable = false)
    private String message;

    @Column(name = "is_read")
    private Boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (type == null) {
            type = "system";
        }
        if (read == null) {
            read = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
