package web.resident.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResidentUpdateRequestDto {

    private String name;
    private String email;
    private String building;
    private String unit;
    private String phone;
    private Integer residentCarLimit;
    private Integer visitorCarLimit;
}
