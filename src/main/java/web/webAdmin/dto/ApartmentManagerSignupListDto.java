package web.webAdmin.dto;

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
public class ApartmentManagerSignupListDto {

    private Integer managerNo;
    private Integer apartmentNo;
    private String apartmentName;
    private String loginId;
    private String email;
    private String phone;
    private String address;
    private String name;
    private String picture;
}
