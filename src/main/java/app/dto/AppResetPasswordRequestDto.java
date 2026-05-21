package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppResetPasswordRequestDto {

    @JsonProperty("u_id")
    private String loginId;

    @JsonProperty("u_dong")
    private String dong;

    @JsonProperty("u_ho")
    private String ho;

    private String newPassword;
}
