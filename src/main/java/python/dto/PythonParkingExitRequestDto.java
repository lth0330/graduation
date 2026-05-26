package python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonParkingExitRequestDto {

    private String zone;

    @JsonProperty("exit_time")
    private String exitTime;
}
