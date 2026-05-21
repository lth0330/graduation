package app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "device_info")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoEntity {

    @Id
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_no", nullable = false)
    private ResidentEntity resident;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "os_type", length = 20)
    private String osType;

    @Column(name = "last_login")
    private LocalDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (lastLoginAt == null) {
            lastLoginAt = LocalDateTime.now();
        }
    }
}
