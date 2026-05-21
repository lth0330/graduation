package app.entity;

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

@Entity
@Table(name = "settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_no")
    private Integer no;

    @Column(name = "device_id", length = 100, nullable = false, unique = true)
    private String deviceId;

    @Column(name = "alert_push")
    private Boolean alertPush;

    @Column(name = "theme_mode", length = 20)
    private String themeMode;
}
