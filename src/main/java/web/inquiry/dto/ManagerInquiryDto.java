package web.inquiry.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ManagerInquiryDto {

    private Integer inquiryNo;
    private Integer managerNo;
    private Integer apartmentNo;
    private String apartmentName;
    private String writer;
    private String title;
    private String category;
    private String content;
    private String status;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}
