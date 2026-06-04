package web.parking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GatePolicyRequestDto {

    private Boolean gateOccupancyBlockEnabled;
    private Boolean gateForceOpenEnabled;
}
