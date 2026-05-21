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
@Table(name = "registered_cars")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredCarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "v_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_no", nullable = false)
    private ResidentEntity resident;

    @Column(name = "c_number", length = 50, nullable = false)
    private String number;

    @Column(name = "reg_time")
    private LocalDateTime registeredAt;

    @Column(name = "park_time")
    private LocalDateTime parkedAt;

    @Column(name = "expire_date")
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }
}
