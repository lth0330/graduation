package web.webAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WebAdminLoginResponseDto {

    private Integer managerNo;
    private String wId;
    private String tokenType;
    private String accessToken;
}
