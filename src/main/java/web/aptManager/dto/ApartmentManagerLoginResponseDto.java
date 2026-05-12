package web.aptManager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import web.common.type.ApprovalStatus;

@Getter
@Builder
@AllArgsConstructor
public class ApartmentManagerLoginResponseDto {

    private Integer managerNo;
    private Integer apartmentNo;
    private String apartmentName;
    private String loginId;
    private String name;
    private ApprovalStatus approvalStatus;
    private String tokenType;
    private String accessToken;
}
