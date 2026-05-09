package web.parking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParkingLotSaveRequestDto {

    private Integer apartmentNo;
    private String name;
    private String floor;
    private Integer totalSpaces;
    private Integer usedSpaces;
}
