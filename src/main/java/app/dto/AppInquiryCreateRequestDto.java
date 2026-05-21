package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppInquiryCreateRequestDto {

    private String title;

    private String content;

    @JsonProperty("c_no")
    private Integer carNo;
}
