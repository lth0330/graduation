package web.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApartmentManagerDashboardSummaryDto {

    private long residentCount;
    private long pendingResidentRequestCount;
    private long vehicleCount;
    private int totalParkingSpaces;
    private int usedParkingSpaces;
    private int parkingUsageRate;
    private long pendingResidentInquiryCount;
}
