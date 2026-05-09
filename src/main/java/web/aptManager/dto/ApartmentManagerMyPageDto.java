package web.aptManager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApartmentManagerMyPageDto {

    private Integer managerNo;
    private String loginId;
    private String email;
    private String phone;
    private String name;
    private Integer apartmentNo;
    private String apartmentName;
    private String address;
    private String detailAddress;
    private String apartmentPassword;
}
