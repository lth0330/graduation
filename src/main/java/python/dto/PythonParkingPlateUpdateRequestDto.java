package python.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonParkingPlateUpdateRequestDto {

    private String zone;
    private String plate;
}
