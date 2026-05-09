package web.parking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleSaveRequestDto {

    private String carNumber;
    private String carType;
    private Integer ownerId;
    private String note;
}
