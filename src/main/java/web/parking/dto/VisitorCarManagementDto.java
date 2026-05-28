package web.parking.dto;

import java.time.LocalDateTime;
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
public class VisitorCarManagementDto {

    private Integer visitorCarNo;
    private String carNumber;
    private Integer ownerId;
    private String ownerName;
    private String building;
    private String unit;
    private LocalDateTime registeredAt;
    private LocalDateTime parkedAt;
    private LocalDateTime expiresAt;
}
