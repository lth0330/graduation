package web.aptManager.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.common.type.ApprovalStatus;

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
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

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
                .approvalStatus(approvalStatus)
                .rejectReason(rejectReason)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }
}
