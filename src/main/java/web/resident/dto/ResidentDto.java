package web.resident.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;
import web.common.type.ApprovalStatus;
import web.resident.entity.ResidentEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDto {

    private Integer residentNo;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registeredAt;
    private String dong;
    private String ho;
    private Integer apartmentNo;
    private ApprovalStatus approvalStatus;
    private String rejectReason;

    public ResidentEntity toEntity(ApartmentEntity apartment) {
        return ResidentEntity.builder()
                .no(residentNo)
                .loginId(loginId)
                .password(password)
                .name(name)
                .email(email)
                .phone(phone)
                .registeredAt(registeredAt)
                .dong(dong)
                .ho(ho)
                .apartment(apartment)
                .approvalStatus(approvalStatus)
                .rejectReason(rejectReason)
                .build();
    }
}
