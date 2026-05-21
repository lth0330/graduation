package app.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppParkingUpdateRequestDto {

    private List<AppParkingUpdateItemDto> updates;
}
