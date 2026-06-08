package web.parking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParkingZoneSaveRequestDto {

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
