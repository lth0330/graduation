package web.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import web.parking.dto.ParkingHistoryDto;

@Getter
@Builder
public class ManagerNotificationDto {

    private Integer notificationNo;
    private Integer managerNo;
    private Integer apartmentNo;
    private String notificationType;
    private String title;
    private String message;
    private String referenceType;
    private Integer referenceId;
    private ParkingHistoryDto parkingHistory;
    private Boolean read;
    private LocalDateTime createdAt;
}
