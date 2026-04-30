package web.aptManager.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ResidentEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDto {

    private Integer residentNo;
    private String loginId;
    private String password;
    private LocalDateTime registeredAt;
    private String dong;
    private String ho;
    private Integer apartmentNo;

    public ResidentEntity toEntity(ApartmentEntity apartment) {
        return ResidentEntity.builder()
                .no(residentNo)
                .loginId(loginId)
                .password(password)
                .registeredAt(registeredAt)
                .dong(dong)
                .ho(ho)
                .apartment(apartment)
                .build();
    }
}
