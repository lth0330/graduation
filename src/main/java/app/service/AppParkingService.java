package app.service;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingZoneRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 앱 주차 서비스: parking_zone 데이터를 앱 주차 화면에 맞는 형태로 조회한다.
public class AppParkingService {

    private final ParkingZoneRepository parkingZoneRepository;

    public Map<String, Object> findParkingZones() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        // 앱은 전체 주차 구역을 한 번에 받아 화면에서 층/슬롯 형태로 배치한다.
        response.put("zones", parkingZoneRepository.findAllByOrderByNoAsc()
                .stream()
                .map(this::toZoneMap)
                .toList());
        return response;
    }

    private Map<String, Object> toZoneMap(ParkingZoneEntity zone) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("floor", zone.getParkingLot() != null ? zone.getParkingLot().getFloor() : "B1");.
        // 👇 하드코딩된 이름 검사 대신, DB의 'zone_type'을 직접 사용하도록 스마트하게 변경!
        item.put("type", "double_lane".equals(zone.getZoneType()) ? "aisle" : "slot");
        item.put("slot", zone.getAreaNumber());
        item.put("status", zone.getStatus());
        item.put("isOccupied", isOccupied(zone.getStatus()));
        item.put("current_car_number", zone.getCurrentCarNumber());
        return item;
    }

    private boolean isAisle(String areaNumber) {
        return areaNumber != null && (areaNumber.contains("\uD1B5\uB85C") || areaNumber.toLowerCase().contains("aisle"));
    }

    private boolean isOccupied(String status) {
        return status != null && (status.equals("occupied") || status.equals("in_use") || status.equals("\uC0AC\uC6A9\uC911"));
    }
}
