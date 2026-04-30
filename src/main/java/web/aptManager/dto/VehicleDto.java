package web.aptManager.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.entity.ResidentEntity;
import web.aptManager.entity.ResidentVehicleEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private Integer vehicleNo;
    private String name;
    private String number;
    private String kind;
    private LocalDateTime registeredAt;
    private Integer residentNo;

    public ResidentVehicleEntity toEntity(ResidentEntity resident) {
        return ResidentVehicleEntity.builder()
                .no(vehicleNo)
                .name(name)
                .number(number)
                .kind(kind)
                .registeredAt(registeredAt)
                .resident(resident)
                .build();
    }
}
