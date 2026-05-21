package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppLoginRequestDto {

    @JsonProperty("u_id")
    private String loginId;

    @JsonProperty("u_pwd")
    private String password;
}
