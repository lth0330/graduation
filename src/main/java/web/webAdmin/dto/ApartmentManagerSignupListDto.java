package web.webAdmin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.common.type.ApprovalStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentManagerSignupListDto {

    private Integer managerNo;
    private Integer apartmentNo;
    private String apartmentName;
    private String loginId;
    private String email;
    private String phone;
    private String address;
    private String name;
    private String picture;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
}
