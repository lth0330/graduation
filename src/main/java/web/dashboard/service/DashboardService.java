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
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.repository.ResidentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 대시보드 서비스: 관리자 화면에 필요한 집계 데이터를 조회한다.
public class DashboardService {

    private static final String STATUS_OCCUPIED = "occupied";
    private static final String ZONE_TYPE_NORMAL = "normal";

    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ManagerInquiryRepository managerInquiryRepository;
    private final ResidentRepository residentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingZoneRepository parkingZoneRepository;
    private final ResidentInquiryRepository residentInquiryRepository;

    public WebAdminDashboardSummaryDto getWebAdminSummary() {
        // Read: 웹 최고 관리자에게 필요한 승인/문의 요약 수치를 계산한다.
        return WebAdminDashboardSummaryDto.builder()
                .pendingSignupCount(apartmentManagerRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .approvedManagerCount(apartmentManagerRepository.countByApprovalStatus(ApprovalStatus.APPROVED))
                .pendingInquiryCount(managerInquiryRepository.countByStatus("pending"))
                .build();
    }

    public ApartmentManagerDashboardSummaryDto getApartmentManagerSummary(Map<String, Object> principal) {
        // Read: 로그인한 아파트 관리자의 아파트 기준 입주민/차량/주차/문의 통계를 계산한다.
        Integer apartmentNo = getInteger(principal, "apartmentNo");
        // 사용률은 실제 주차면 상태로 계산한다. 차단기 만차 판단(PythonGateService)과 같은 기준: 일반 주차면만.
        // 예전에는 주차장에 적어 둔 total_spaces(통로 칸 포함)와 used_spaces(일반 칸만)를 섞어 써서
        // 주차 현황 화면·차단기 판단과 숫자가 어긋났음.
        List<ParkingZoneEntity> normalZones = parkingLotRepository.findByApartment_No(apartmentNo).stream()
                .map(ParkingLotEntity::getNo)
                .filter(parkingLotNo -> parkingLotNo != null)
                .flatMap(parkingLotNo -> parkingZoneRepository.findByParkingLot_No(parkingLotNo).stream())
                .filter(this::isNormalZone)
                .toList();
        int totalParkingSpaces = normalZones.size();
        int usedParkingSpaces = (int) normalZones.stream()
                .filter(zone -> STATUS_OCCUPIED.equals(zone.getStatus()))
                .count();
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

    private boolean isNormalZone(ParkingZoneEntity zone) {
        String zoneType = zone.getZoneType();
        return zoneType == null || zoneType.isBlank() || ZONE_TYPE_NORMAL.equals(zoneType);
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
