package web.aptManager.dto;

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
public class ApartmentInfoDto {

    private Integer apartmentNo;
    private String apartmentName;
    private String apartmentPassword;
    private String address;
    private String detailAddress;

    public ApartmentEntity toEntity() {
        return ApartmentEntity.builder()
                .no(apartmentNo)
                .name(apartmentName)
                .password(apartmentPassword)
                .address(address)
                .detailAddress(detailAddress)
                .build();
    }
}
