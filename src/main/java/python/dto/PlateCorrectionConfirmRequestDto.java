package python.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlateCorrectionConfirmRequestDto {

    @JsonAlias({"plate", "c_number", "gate_plate"})
    private String plate;
}
