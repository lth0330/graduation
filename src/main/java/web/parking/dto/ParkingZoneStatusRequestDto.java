package web.parking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParkingZoneStatusRequestDto {

    private String status;
    private String zoneType;
    private String statusChangeReason;
}
