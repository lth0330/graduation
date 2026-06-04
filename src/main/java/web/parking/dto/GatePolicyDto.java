package web.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatePolicyDto {

    private Integer apartmentNo;
    private Boolean gateOccupancyBlockEnabled;
    private Boolean gateForceOpenEnabled;
}
