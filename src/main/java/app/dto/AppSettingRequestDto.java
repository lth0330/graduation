package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppSettingRequestDto {

    @JsonProperty("alert_push")
    private Boolean alertPush;

    @JsonProperty("theme_mode")
    private String themeMode;
}
