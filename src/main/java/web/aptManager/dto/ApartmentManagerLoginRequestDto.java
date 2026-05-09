package web.aptManager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApartmentManagerLoginRequestDto {

    private String loginId;
    private String password;
}
