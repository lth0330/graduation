package python.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonGateCheckRequestDto {

    private String plate;

    @JsonAlias({"apartment_no", "a_no"})
    private Integer apartmentNo;
}
