package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppDeviceTokenRequestDto {

    @JsonProperty("fcm_token")
    private String fcmToken;
}
