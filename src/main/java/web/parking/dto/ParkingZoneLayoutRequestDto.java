package web.parking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParkingZoneLayoutRequestDto {

    private Integer layoutRow;
    private Integer layoutColumn;
}
