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
        item.put("floor", zone.getParkingLot() != null ? zone.getParkingLot().getFloor() : "B1");
        // 👇 하드코딩된 이름 검사 대신, DB의 'zone_type'을 직접 사용하도록 스마트하게 변경!
        item.put("type", "double_lane".equals(zone.getZoneType()) ? "aisle" : "slot");
        item.put("slot", zone.getAreaNumber());
        item.put("status", zone.getStatus());
        item.put("isOccupied", isOccupied(zone.getStatus()));
        item.put("current_car_number", toAppCarNumber(zone));
        // 👇 [핵심 추가] 관리자가 웹에서 지정한 행(row)과 열(column) 위치를 앱으로 전달합니다!
        item.put("layout_row", zone.getLayoutRow());
        item.put("layout_column", zone.getLayoutColumn());
                // 👇 아래 두 줄을 추가하세요
        item.put("layout_width", zone.getLayoutWidth());
        item.put("layout_height", zone.getLayoutHeight());
        return item;
    }

    // 관리자가 웹에서 '주차됨'·'사용 불가'로 바꾼 칸은 차 번호가 없다(null).
    // 앱은 "칸 번호 == 내 차 번호"로 내 차를 찾는데, 차가 없는 입주민은 내 차 번호도 null이라
    // null == null 로 그 칸이 '내 차'로 보였다 → 빈 칸이 아닌데 번호가 없으면 빈 글자를 보낸다.
    private String toAppCarNumber(ParkingZoneEntity zone) {
        String carNumber = zone.getCurrentCarNumber();
        if (carNumber == null && zone.getStatus() != null && !"empty".equals(zone.getStatus())) {
            return "";
        }
        return carNumber;
    }

    private boolean isAisle(String areaNumber) {
        return areaNumber != null && (areaNumber.contains("\uD1B5\uB85C") || areaNumber.toLowerCase().contains("aisle"));
    }

    private boolean isOccupied(String status) {
        return status != null && (status.equals("occupied") || status.equals("in_use") || status.equals("\uC0AC\uC6A9\uC911"));
    }
}
