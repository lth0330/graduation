package web.parking.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VehicleManagementDto {

    private Integer vehicleNo;
    private String carNumber;
    private String carType;
    private Integer ownerId;
    private String ownerName;
    private String building;
    private String unit;
    private String note;
    private LocalDateTime registeredAt;
}
