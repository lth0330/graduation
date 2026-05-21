package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppWaitlistRequestDto {

    @JsonProperty("target_slot_id")
    private String targetSlotId;
}
