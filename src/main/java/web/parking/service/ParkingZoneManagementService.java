package web.parking.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 👇 [핵심 1] 알림 발송에 필요한 앱 쪽 파일들을 모두 가져옵니다!
import app.entity.AppNotificationEntity;
import app.entity.AppSettingEntity;
import app.repository.AppNotificationRepository;
import app.repository.AppSettingRepository;
import app.repository.DeviceInfoRepository;
import app.repository.WaitingListRepository;
import app.service.FcmService;

import web.parking.dto.ParkingZoneDto;
import web.parking.dto.ParkingZoneLayoutRequestDto;
import web.parking.dto.ParkingZoneSaveRequestDto;
import web.parking.dto.ParkingZoneStatusRequestDto;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingLotRepository;
import web.parking.repository.ParkingZoneRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 웹 주차구역 관리 서비스: parking_zone 테이블의 슬롯 CRUD와 상태/배치 수정을 처리한다.
public class ParkingZoneManagementService {

    private static final int DEFAULT_LAYOUT_WIDTH = 2;
    private static final int DEFAULT_LAYOUT_HEIGHT = 1;

    private final ParkingZoneRepository parkingZoneRepository;
    private final ParkingLotRepository parkingLotRepository;

    // 👇 [핵심 2] 알림을 보내기 위해 필요한 부품(도구)들을 조립해 줍니다!
    private final WaitingListRepository waitingListRepository;
    private final AppNotificationRepository notificationRepository;
    private final AppSettingRepository settingRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final FcmService fcmService;

    public List<ParkingZoneDto> findParkingZones(Integer parkingLotNo) {
        // Read: 주차장 번호로 주차구역 목록을 조회한다.
        return parkingZoneRepository.findByParkingLot_No(parkingLotNo)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ParkingZoneDto create(ParkingZoneSaveRequestDto requestDto) {
        // Create: 주차장에 연결된 주차구역을 등록한다.
        validateSaveRequest(requestDto);
        Integer layoutWidth = normalizeLayoutSize(requestDto.getLayoutWidth(), DEFAULT_LAYOUT_WIDTH);
        Integer layoutHeight = normalizeLayoutSize(requestDto.getLayoutHeight(), DEFAULT_LAYOUT_HEIGHT);
        validateOverlappingPlacement(
                requestDto.getParkingLotNo(),
                requestDto.getLayoutRow(),
                requestDto.getLayoutColumn(),
                layoutWidth,
                layoutHeight,
                null
        );

        ParkingLotEntity parkingLot = findParkingLot(requestDto.getParkingLotNo());
        ParkingZoneEntity parkingZone = ParkingZoneEntity.builder()
                .parkingLot(parkingLot)
                .areaNumber(requestDto.getAreaNumber())
                .location(requestDto.getLocation())
                .status(normalizeStatus(requestDto.getStatus()))
                .zoneType(normalizeZoneType(requestDto.getZoneType()))
                .layoutRow(requestDto.getLayoutRow())
                .layoutColumn(requestDto.getLayoutColumn())
                .layoutWidth(layoutWidth)
                .layoutHeight(layoutHeight)
                .statusChangeReason(requestDto.getStatusChangeReason())
                .build();

        return toDto(parkingZoneRepository.save(parkingZone));
    }

    @Transactional
    public ParkingZoneDto updateStatus(Integer parkingZoneNo, ParkingZoneStatusRequestDto requestDto) {
        // Update: 주차구역의 점유 상태와 상태 변경 사유를 수정한다.
        if (requestDto == null || isBlank(requestDto.getStatus()) || isBlank(requestDto.getStatusChangeReason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상태와 변경 사유를 입력해주세요.");
        }

        ParkingZoneEntity parkingZone = findEntity(parkingZoneNo);
        
        // 💡 [핵심 3] 상태가 바뀌기 전의 과거 상태를 기억해 둡니다.
        String oldStatus = parkingZone.getStatus();
        String newStatus = normalizeStatus(requestDto.getStatus());

        // 상태 업데이트 적용
        parkingZone.setStatus(newStatus);
        if (!isBlank(requestDto.getZoneType())) {
            parkingZone.setZoneType(normalizeZoneType(requestDto.getZoneType()));
        }
        parkingZone.setStatusChangeReason(requestDto.getStatusChangeReason());

        // =================================================================
        // 💡 [핵심 4] 웹 관리자가 수동으로 'empty'로 바꿨을 때 알림 발송 로직!
        // =================================================================
        if (!"empty".equals(oldStatus) && "empty".equals(newStatus)) {
            String slotName = parkingZone.getAreaNumber(); // 변경된 주차구역 이름

            waitingListRepository.findAll().stream()
                    .filter(w -> !w.getNotified() && (w.getTargetSlotId().equals("ALL") || w.getTargetSlotId().equals(slotName)))
                    .forEach(w -> {
                        String msg = "대기하시던 [" + slotName + "] 구역에 빈자리가 생겼습니다! 먼저 주차하세요.";
                        w.setNotified(true); // 대기권 사용 처리

                        // 1. 앱 내부 알림함에 저장
                        notificationRepository.save(AppNotificationEntity.builder()
                                .resident(w.getResident()).type("system").title("🔔 빈자리 알림").message(msg).read(false).build());

                        // 2. 스마트폰으로 푸시 알림 쏘기
                        boolean isPushOn = settingRepository.findByDeviceId("device_" + w.getResident().getNo())
                                .map(AppSettingEntity::getAlertPush).orElse(true);
                        if (isPushOn) {
                            deviceInfoRepository.findByResident_No(w.getResident().getNo())
                                    .forEach(d -> fcmService.sendPush(d.getFcmToken(), "🔔 빈자리 알림", msg));
                        }
                    });
        }
        // =================================================================

        return toDto(parkingZone);
    }

    @Transactional
    public ParkingZoneDto updateLayout(Integer parkingZoneNo, ParkingZoneLayoutRequestDto requestDto) {
        // Update: 관리자 화면에서 사용하는 주차구역 배치 좌표를 수정한다.
        if (requestDto == null || requestDto.getLayoutRow() == null || requestDto.getLayoutColumn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 행과 열을 입력해주세요.");
        }
        if (requestDto.getLayoutRow() < 1 || requestDto.getLayoutColumn() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 행과 열은 1 이상이어야 합니다.");
        }

        ParkingZoneEntity parkingZone = findEntity(parkingZoneNo);
        Integer layoutWidth = normalizeLayoutSize(
                requestDto.getLayoutWidth(),
                normalizeLayoutSize(parkingZone.getLayoutWidth(), DEFAULT_LAYOUT_WIDTH)
        );
        Integer layoutHeight = normalizeLayoutSize(
                requestDto.getLayoutHeight(),
                normalizeLayoutSize(parkingZone.getLayoutHeight(), DEFAULT_LAYOUT_HEIGHT)
        );
        if (layoutWidth < 1 || layoutHeight < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 가로와 세로는 1 이상이어야 합니다.");
        }

        validateOverlappingPlacement(
                parkingZone.getParkingLot().getNo(),
                requestDto.getLayoutRow(),
                requestDto.getLayoutColumn(),
                layoutWidth,
                layoutHeight,
                parkingZoneNo
        );

        parkingZone.setLayoutRow(requestDto.getLayoutRow());
        parkingZone.setLayoutColumn(requestDto.getLayoutColumn());
        parkingZone.setLayoutWidth(layoutWidth);
        parkingZone.setLayoutHeight(layoutHeight);
        return toDto(parkingZone);
    }

    @Transactional
    public void delete(Integer parkingZoneNo) {
        // Delete: 주차구역을 삭제한다.
        parkingZoneRepository.delete(findEntity(parkingZoneNo));
    }

    private ParkingZoneEntity findEntity(Integer parkingZoneNo) {
        return parkingZoneRepository.findById(parkingZoneNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주차구역입니다."));
    }

    private ParkingLotEntity findParkingLot(Integer parkingLotNo) {
        return parkingLotRepository.findById(parkingLotNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 주차장입니다."));
    }

    private void validateSaveRequest(ParkingZoneSaveRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주차구역 정보를 입력해주세요.");
        }
        if (requestDto.getParkingLotNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주차장 번호는 필수입니다.");
        }
        if (isBlank(requestDto.getAreaNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "구역 번호는 필수입니다.");
        }
        if (isBlank(requestDto.getLocation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "위치는 필수입니다.");
        }
        if (requestDto.getLayoutRow() == null || requestDto.getLayoutColumn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 행과 열은 필수입니다.");
        }
        if (requestDto.getLayoutRow() < 1 || requestDto.getLayoutColumn() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 행과 열은 1 이상이어야 합니다.");
        }
        Integer layoutWidth = normalizeLayoutSize(requestDto.getLayoutWidth(), DEFAULT_LAYOUT_WIDTH);
        Integer layoutHeight = normalizeLayoutSize(requestDto.getLayoutHeight(), DEFAULT_LAYOUT_HEIGHT);
        if (layoutWidth < 1 || layoutHeight < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 가로와 세로는 1 이상이어야 합니다.");
        }
    }

    private void validateOverlappingPlacement(
            Integer parkingLotNo,
            Integer layoutRow,
            Integer layoutColumn,
            Integer layoutWidth,
            Integer layoutHeight,
            Integer currentZoneNo
    ) {
        boolean overlaps = parkingZoneRepository.findByParkingLot_No(parkingLotNo)
                .stream()
                .filter(zone -> currentZoneNo == null || !currentZoneNo.equals(zone.getNo()))
                .anyMatch(zone -> overlaps(
                        layoutRow,
                        layoutColumn,
                        layoutWidth,
                        layoutHeight,
                        zone
                ));

        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 배치 영역입니다.");
        }
    }

    private boolean overlaps(
            Integer layoutRow,
            Integer layoutColumn,
            Integer layoutWidth,
            Integer layoutHeight,
            ParkingZoneEntity existing
    ) {
        if (existing.getLayoutRow() == null || existing.getLayoutColumn() == null) {
            return false;
        }

        int rowStart = layoutRow;
        int rowEnd = layoutRow + layoutHeight - 1;
        int columnStart = layoutColumn;
        int columnEnd = layoutColumn + layoutWidth - 1;
        int existingRowStart = existing.getLayoutRow();
        int existingRowEnd = existing.getLayoutRow()
                + normalizeLayoutSize(existing.getLayoutHeight(), DEFAULT_LAYOUT_HEIGHT) - 1;
        int existingColumnStart = existing.getLayoutColumn();
        int existingColumnEnd = existing.getLayoutColumn()
                + normalizeLayoutSize(existing.getLayoutWidth(), DEFAULT_LAYOUT_WIDTH) - 1;

        boolean rowOverlaps = rowStart <= existingRowEnd && rowEnd >= existingRowStart;
        boolean columnOverlaps = columnStart <= existingColumnEnd && columnEnd >= existingColumnStart;
        return rowOverlaps && columnOverlaps;
    }

    private String normalizeStatus(String status) {
        if (isBlank(status)) {
            return "empty";
        }
        if (!List.of("empty", "occupied", "disabled").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 주차구역 상태입니다.");
        }
        return status;
    }

    private String normalizeZoneType(String zoneType) {
        if (isBlank(zoneType)) {
            return "normal";
        }
        if (!List.of("normal", "double_lane").contains(zoneType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 주차구역 종류입니다.");
        }
        return zoneType;
    }

    private ParkingZoneDto toDto(ParkingZoneEntity parkingZone) {
        return ParkingZoneDto.builder()
                .parkingZoneNo(parkingZone.getNo())
                .parkingLotNo(parkingZone.getParkingLot() != null ? parkingZone.getParkingLot().getNo() : null)
                .areaNumber(parkingZone.getAreaNumber())
                .location(parkingZone.getLocation())
                .status(parkingZone.getStatus())
                .zoneType(parkingZone.getZoneType())
                .layoutRow(parkingZone.getLayoutRow())
                .layoutColumn(parkingZone.getLayoutColumn())
                .layoutWidth(normalizeLayoutSize(parkingZone.getLayoutWidth(), DEFAULT_LAYOUT_WIDTH))
                .layoutHeight(normalizeLayoutSize(parkingZone.getLayoutHeight(), DEFAULT_LAYOUT_HEIGHT))
                .statusChangeReason(parkingZone.getStatusChangeReason())
                .build();
    }

    private Integer normalizeLayoutSize(Integer value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}