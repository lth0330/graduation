package web.aptManager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.dto.ApartmentInfoDto;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_no")
    private Integer no;

    @Column(name = "a_name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "a_pwd", length = 30, nullable = false, unique = true)
    private String password;

    @Column(name = "a_address", nullable = false)
    private String address;

    @Column(name = "a_detail_address")
    private String detailAddress;

    @Column(name = "gate_occupancy_block_enabled", nullable = false)
    private Boolean gateOccupancyBlockEnabled;

    @PrePersist
    public void prePersist() {
        if (gateOccupancyBlockEnabled == null) {
            gateOccupancyBlockEnabled = true;
        }
    }

    public ApartmentInfoDto toDTO() {
        return ApartmentInfoDto.builder()
                .apartmentNo(no)
                .apartmentName(name)
                .apartmentPassword(password)
                .address(address)
                .detailAddress(detailAddress)
                .build();
    }
}
