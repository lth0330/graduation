package python.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gate_entry_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateEntryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_no")
    private Integer logNo;

    @Column(name = "gate_plate", length = 20, nullable = false)
    private String plate;

    @Column(name = "gate_is_resident")
    private Boolean resident;

    @Column(name = "gate_open")
    private Boolean gateOpen;

    @Column(name = "gate_time")
    private LocalDateTime gateTime;

    @PrePersist
    public void prePersist() {
        if (resident == null) {
            resident = false;
        }
        if (gateOpen == null) {
            gateOpen = false;
        }
        if (gateTime == null) {
            gateTime = LocalDateTime.now();
        }
    }
}
