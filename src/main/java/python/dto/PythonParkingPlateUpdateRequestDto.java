package python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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

    @JsonProperty("ocr_plate")
    private String ocrPlate;

    @JsonProperty("matched_plate")
    private String matchedPlate;

    @JsonProperty("candidate_list")
    private List<String> candidateList;

    private Integer distance;

    @JsonProperty("auto_confirmed")
    private Boolean autoConfirmed;

    @JsonProperty("needs_review")
    private Boolean needsReview;
}
