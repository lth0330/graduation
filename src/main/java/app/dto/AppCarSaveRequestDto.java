package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppCarSaveRequestDto {

    @JsonProperty("c_number")
    private String number;

    @JsonProperty("c_name")
    private String name;

    @JsonProperty("car_type")
    private String carType;

    @JsonProperty("c_note")
    private String note;
}
