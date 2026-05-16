package web.inquiry.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResidentInquiryCreateRequestDto {

    private Integer residentNo;
    private Integer vehicleNo;
    private String title;
    private String content;
}
