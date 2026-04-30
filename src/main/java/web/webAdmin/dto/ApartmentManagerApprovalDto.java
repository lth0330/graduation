package web.webAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentManagerApprovalDto {

    private Integer managerNo;
    private Integer apartmentNo;
    private String apartmentPassword;

    public ApartmentEntity toApartmentEntity(String apartmentName) {
        return ApartmentEntity.builder()
                .no(apartmentNo)
                .name(apartmentName)
                .password(apartmentPassword)
                .build();
    }
}
