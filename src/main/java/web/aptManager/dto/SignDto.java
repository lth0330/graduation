package web.aptManager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignDto {

    private Integer managerNo;
    private Integer apartmentNo;
    private String loginId;
    private String password;
    private String email;
    private String phone;
    private String address;
    private String name;
    private String picture;

    public ApartmentManagerEntity toEntity(ApartmentEntity apartment) {
        return ApartmentManagerEntity.builder()
                .no(managerNo)
                .apartment(apartment)
                .loginId(loginId)
                .password(password)
                .email(email)
                .phone(phone)
                .address(address)
                .name(name)
                .picture(picture)
                .build();
    }
}
