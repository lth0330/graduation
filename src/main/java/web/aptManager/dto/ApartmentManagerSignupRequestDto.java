package web.aptManager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApartmentManagerSignupRequestDto {

    private String loginId;
    private String password;
    private String email;
    private String phone;
    private String name;
    private String apartmentName;
    private String address;
    private String detailAddress;
    private String careerImage;
}
