package web.webAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.webAdmin.entity.WebManagerEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebManagerDto {

    private Integer managerNo;
    private String loginId;
    private String password;

    public WebManagerEntity toEntity() {
        return WebManagerEntity.builder()
                .no(managerNo)
                .loginId(loginId)
                .password(password)
                .build();
    }
}
