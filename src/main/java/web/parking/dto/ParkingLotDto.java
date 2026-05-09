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
public class ParkingLotDto {

    private Integer parkingLotNo;
    private Integer apartmentNo;
    private String name;
    private String floor;
    private Integer totalSpaces;
    private Integer usedSpaces;
}
