package web.inquiry.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ManagerInquiryCreateRequestDto {

    private String title;
    private String category;
    private String content;
}
