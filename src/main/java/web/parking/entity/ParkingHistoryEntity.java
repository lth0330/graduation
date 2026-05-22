package web.parking.entity;

import app.entity.RegisteredCarEntity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "parking_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pz_no")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ParkingZoneEntity parkingZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_no")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ResidentVehicleEntity residentVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "v_no")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RegisteredCarEntity visitorVehicle;

    @Column(name = "history_zone", length = 255, nullable = false)
    private String zoneSnapshot;

    @Column(name = "history_plate", length = 50, nullable = false)
    private String plate;

    @Column(name = "history_entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "history_exit_time")
    private LocalDateTime exitTime;

    @Column(name = "history_status", length = 20, nullable = false)
    private String status;

    @PrePersist
    public void prePersist() {
        if (entryTime == null) {
            entryTime = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "PARKED";
        }
    }
}
