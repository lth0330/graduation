package web.resident.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResidentCreateRequestDto {

    private Integer apartmentNo;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String building;
    private String unit;
    private String phone;
}
