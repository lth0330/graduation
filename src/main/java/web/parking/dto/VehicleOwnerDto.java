package web.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VehicleOwnerDto {

    private Integer residentNo;
    private String name;
    private String building;
    private String unit;
    private String phone;
    private String carNumber;
    private String vehicleType;
}
