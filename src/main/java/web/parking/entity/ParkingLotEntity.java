package web.parking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;

@Entity
@Table(name = "parking_lot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pl_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_no", nullable = false)
    private ApartmentEntity apartment;

    @Column(name = "pl_name", nullable = false)
    private String name;

    @Column(name = "pl_floor", length = 20, nullable = false)
    private String floor;

    @Column(name = "total_spaces", nullable = false)
    private Integer totalSpaces;

    @Column(name = "used_spaces", nullable = false)
    private Integer usedSpaces;
}
