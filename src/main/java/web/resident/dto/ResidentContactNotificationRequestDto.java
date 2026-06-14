package web.resident.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResidentContactNotificationRequestDto {

    private String title;
    private String message;
    private String type;
}
