package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppFindIdRequestDto {

    @JsonProperty("u_dong")
    private String dong;

    @JsonProperty("u_ho")
    private String ho;

    @JsonProperty("apt_pwd")
    private String apartmentPassword;
}
