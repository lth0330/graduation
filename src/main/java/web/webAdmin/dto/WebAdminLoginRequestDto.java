package web.webAdmin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WebAdminLoginRequestDto {

    private String wId;
    private String wPwd;
}
