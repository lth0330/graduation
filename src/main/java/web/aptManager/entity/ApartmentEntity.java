package web.aptManager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    public ApartmentInfoDto toDTO() {
        return ApartmentInfoDto.builder()
                .apartmentNo(no)
                .apartmentName(name)
                .apartmentPassword(password)
                .build();
    }
}
