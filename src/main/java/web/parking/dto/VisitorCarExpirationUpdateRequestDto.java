package web.parking.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VisitorCarExpirationUpdateRequestDto {

    private LocalDateTime expiresAt;
}
