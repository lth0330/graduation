package python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonParkingEntryRequestDto {

    private String zone;
    private String plate;

    @JsonProperty("park_type")
    private String parkType;

    @JsonProperty("linked_zone")
    private String linkedZone;

    @JsonProperty("entry_time")
    private String entryTime;

    @JsonProperty("image_path")
    private String imagePath;

    @JsonProperty("image_base64")
    private String imageBase64;
}
