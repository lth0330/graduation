package web.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WebAdminDashboardSummaryDto {

    private long pendingSignupCount;
    private long approvedManagerCount;
    private long pendingInquiryCount;
}
