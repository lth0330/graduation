package web.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import web.aptManager.repository.ApartmentManagerRepository;
import web.dashboard.dto.ApartmentManagerDashboardSummaryDto;
import web.inquiry.repository.ManagerInquiryRepository;
import web.inquiry.repository.ResidentInquiryRepository;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.repository.ResidentRepository;

class DashboardServiceTest {

    @Test
    void parkingUsageUsesActualNormalZoneStatusesNotStoredLotNumbers() {
        ParkingLotRepository parkingLotRepository = mock(ParkingLotRepository.class);
        ParkingZoneRepository parkingZoneRepository = mock(ParkingZoneRepository.class);

        // 저장된 숫자(전체 9, 사용 2)는 실제 칸 상태와 다르다
        ParkingLotEntity lot = ParkingLotEntity.builder().no(1).totalSpaces(9).usedSpaces(2).build();
        when(parkingLotRepository.findByApartment_No(1)).thenReturn(List.of(lot));
        when(parkingZoneRepository.findByParkingLot_No(1)).thenReturn(List.of(
                zone("a-b1-001", "normal", "occupied"),
                zone("a-b1-002", "normal", "occupied"),
                zone("a-b1-003", "normal", "occupied"),
                zone("a-b1-004", "normal", "empty"),
                zone("a-b1-005", "normal", "reserved"),
                zone("a-b1-006", "normal", "empty"),
                zone("a-b1-007", "double_lane", "occupied"),
                zone("a-b1-008", "double_lane", "empty"),
                zone("a-b1-009", "double_lane", "empty")
        ));

        DashboardService service = new DashboardService(
                mock(ApartmentManagerRepository.class),
                mock(ManagerInquiryRepository.class),
                mock(ResidentRepository.class),
                mock(ResidentVehicleRepository.class),
                parkingLotRepository,
                parkingZoneRepository,
                mock(ResidentInquiryRepository.class)
        );

        ApartmentManagerDashboardSummaryDto summary = service.getApartmentManagerSummary(Map.of("apartmentNo", 1));

        assertThat(summary.getTotalParkingSpaces()).isEqualTo(6);
        assertThat(summary.getUsedParkingSpaces()).isEqualTo(3);
        assertThat(summary.getParkingUsageRate()).isEqualTo(50);
    }

    private ParkingZoneEntity zone(String areaNumber, String zoneType, String status) {
        return ParkingZoneEntity.builder().areaNumber(areaNumber).zoneType(zoneType).status(status).build();
    }
}
