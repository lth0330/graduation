package python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonParkingPlateUpdateRequestDto {

    private String zone;
    private String plate;

    @JsonProperty("image_path")
    private String imagePath;

    @JsonProperty("image_base64")
    private String imageBase64;
}
