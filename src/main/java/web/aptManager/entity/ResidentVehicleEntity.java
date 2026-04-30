package web.aptManager.entity;

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
import web.aptManager.dto.VehicleDto;

@Entity
@Table(name = "car")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentVehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_no")
    private Integer no;

    @Column(name = "c_name", length = 30, nullable = false)
    private String name;

    @Column(name = "c_number", nullable = false)
    private String number;

    @Column(name = "c_kind", length = 30)
    private String kind;

    @Column(name = "c_date")
    private LocalDateTime registeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_no")
    private ResidentEntity resident;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }

    public VehicleDto toDTO() {
        return VehicleDto.builder()
                .vehicleNo(no)
                .name(name)
                .number(number)
                .kind(kind)
                .registeredAt(registeredAt)
                .residentNo(resident != null ? resident.getNo() : null)
                .build();
    }
}
