package web.resident.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import web.common.type.ApprovalStatus;

@Getter
@Builder
@AllArgsConstructor
public class ResidentManagementDto {

    private Integer residentNo;
    private Integer apartmentNo;
    private String name;
    private String loginId;
    private String email;
    private String building;
    private String unit;
    private String phone;
    private Integer vehicleCount;
    private Integer residentCarLimit;
    private Integer visitorCarLimit;
    private LocalDateTime joinedAt;
    private ApprovalStatus approvalStatus;
}
