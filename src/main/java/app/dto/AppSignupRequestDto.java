package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppSignupRequestDto {

    @JsonProperty("u_id")
    private String loginId;

    @JsonProperty("u_pwd")
    private String password;

    @JsonProperty("u_name")
    private String name;

    @JsonProperty("u_email")
    private String email;

    @JsonProperty("u_phone")
    private String phone;

    @JsonProperty("u_dong")
    private String dong;

    @JsonProperty("u_ho")
    private String ho;

    @JsonProperty("a_no")
    private Integer apartmentNo;

    @JsonProperty("a_pwd")
    private String apartmentPassword;
}
