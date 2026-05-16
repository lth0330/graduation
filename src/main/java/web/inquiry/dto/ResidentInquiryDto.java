package web.inquiry.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResidentInquiryDto {

    private Integer inquiryNo;
    private Integer residentNo;
    private Integer apartmentNo;
    private String writer;
    private String building;
    private String unit;
    private Integer vehicleNo;
    private String carNumber;
    private String title;
    private String content;
    private String status;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}
