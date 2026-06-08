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
public class ParkingZoneDto {

    private Integer parkingZoneNo;
    private Integer parkingLotNo;
    private String areaNumber;
    private String location;
    private String status;
    private String zoneType;
    private Integer layoutRow;
    private Integer layoutColumn;
    private Integer layoutWidth;
    private Integer layoutHeight;
    private String statusChangeReason;
}
