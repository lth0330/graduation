package web.resident.dto;

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
public class ResidentApprovalDto {

    private Integer residentNo;
    private Integer apartmentNo;
    private String name;
    private String loginId;
    private String email;
    private String building;
    private String unit;
    private String carNumber;
    private String carType;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private LocalDateTime requestedAt;
}
