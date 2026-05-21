package app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppParkingUpdateItemDto {

    private String slot;
    private String status;
}
