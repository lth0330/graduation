package web.dashboard.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.repository.ApartmentManagerRepository;
import web.common.type.ApprovalStatus;
import web.dashboard.dto.ApartmentManagerDashboardSummaryDto;
import web.dashboard.dto.WebAdminDashboardSummaryDto;
import web.inquiry.repository.ManagerInquiryRepository;
import web.inquiry.repository.ResidentInquiryRepository;
import web.parking.entity.ParkingLotEntity;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ManagerInquiryRepository managerInquiryRepository;
    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ResidentInquiryRepository residentInquiryRepository;

    public WebAdminDashboardSummaryDto getWebAdminSummary() {
        return WebAdminDashboardSummaryDto.builder()
                .pendingSignupCount(apartmentManagerRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .approvedManagerCount(apartmentManagerRepository.countByApprovalStatus(ApprovalStatus.APPROVED))
                .pendingInquiryCount(managerInquiryRepository.countByStatus("pending"))
                .build();
    }

    public ApartmentManagerDashboardSummaryDto getApartmentManagerSummary(Map<String, Object> principal) {
        Integer apartmentNo = getInteger(principal, "apartmentNo");
        List<ParkingLotEntity> parkingLots = parkingLotRepository.findByApartment_No(apartmentNo);
        int totalParkingSpaces = parkingLots.stream()
                .map(ParkingLotEntity::getTotalSpaces)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
        int usedParkingSpaces = parkingLots.stream()
                .map(ParkingLotEntity::getUsedSpaces)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
        int parkingUsageRate = totalParkingSpaces > 0
                ? Math.round((usedParkingSpaces * 100.0f) / totalParkingSpaces)
                : 0;

        return ApartmentManagerDashboardSummaryDto.builder()
                .residentCount(residentRepository.countByApartment_NoAndApprovalStatus(apartmentNo, ApprovalStatus.APPROVED))
                .pendingResidentRequestCount(residentRepository.countByApartment_NoAndApprovalStatus(apartmentNo, ApprovalStatus.PENDING))
                .vehicleCount(residentVehicleRepository.countByResident_Apartment_No(apartmentNo))
                .totalParkingSpaces(totalParkingSpaces)
                .usedParkingSpaces(usedParkingSpaces)
                .parkingUsageRate(parkingUsageRate)
                .pendingResidentInquiryCount(residentInquiryRepository.countByResident_Apartment_NoAndStatus(apartmentNo, "pending"))
                .build();
    }

    private Integer getInteger(Map<String, Object> principal, String key) {
        Object value = principal != null ? principal.get(key) : null;

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }
}
