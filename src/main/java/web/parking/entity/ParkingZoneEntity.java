package web.parking.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parking_zone")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pz_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pl_no", nullable = false)
    private ParkingLotEntity parkingLot;

    @Column(name = "area_number", nullable = false)
    private String areaNumber;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "zone_type", length = 30)
    private String zoneType;

    @Column(name = "layout_row")
    private Integer layoutRow;

    @Column(name = "layout_column")
    private Integer layoutColumn;

    @Column(name = "layout_width")
    private Integer layoutWidth;

    @Column(name = "layout_height")
    private Integer layoutHeight;

    @Column(name = "status_change_reason", length = 255)
    private String statusChangeReason;

    @Column(name = "current_car_number")
    private String currentCarNumber;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) {
            status = "empty";
        }
        if (zoneType == null || zoneType.isBlank()) {
            zoneType = "normal";
        }
        if (layoutWidth == null || layoutWidth < 1) {
            layoutWidth = 2;
        }
        if (layoutHeight == null || layoutHeight < 1) {
            layoutHeight = 1;
        }
    }
}
