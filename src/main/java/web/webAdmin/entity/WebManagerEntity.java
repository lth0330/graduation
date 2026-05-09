package web.webAdmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.webAdmin.dto.WebManagerDto;

@Entity
@Table(name = "web_manager")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebManagerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "w_no")
    private Integer no;

    @Column(name = "w_id", nullable = false, unique = true)
    private String wId;

    @Column(name = "w_pwd", length = 100, nullable = false, unique = true)
    private String wPwd;

    public WebManagerDto toDTO() {
        return WebManagerDto.builder()
                .managerNo(no)
                .wId(wId)
                .build();
    }
}
