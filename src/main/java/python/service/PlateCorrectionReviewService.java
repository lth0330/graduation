package python.service;

import app.entity.RegisteredCarEntity;
import app.repository.RegisteredCarRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import python.dto.PlateCorrectionReviewDto;
import python.entity.PlateCorrectionReviewEntity;
import python.repository.PlateCorrectionReviewRepository;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.notification.service.ManagerNotificationService;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.entity.ResidentVehicleEntity;
import web.parking.repository.ParkingZoneRepository;
import web.parking.repository.ResidentVehicleRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlateCorrectionReviewService {

    private static final String STATUS_NEEDS_REVIEW = "NEEDS_REVIEW";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String REFERENCE_TYPE = "plate_correction_review";

    private final PlateCorrectionReviewRepository reviewRepository;
    private final ManagerNotificationService managerNotificationService;
    private final ApartmentManagerRepository apartmentManagerRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;
    private final ParkingZoneRepository parkingZoneRepository;

    @Transactional
    public PlateCorrectionReviewEntity recordNeedsReview(
            ApartmentEntity apartment,
            ParkingHistoryEntity history,
            String zone,
            String ocrPlate,
            String matchedPlate,
            List<String> candidateList,
            Integer distance
    ) {
        if (apartment == null || apartment.getNo() == null || history == null || history.getId() == null) {
            return null;
        }

        PlateCorrectionReviewEntity review = reviewRepository
                .findFirstByParkingHistory_IdAndStatusOrderByCreatedAtDesc(history.getId(), STATUS_NEEDS_REVIEW)
                .orElseGet(PlateCorrectionReviewEntity::new);

        review.setApartment(apartment);
        review.setParkingHistory(history);
        review.setZone(limit(zone, 50));
        review.setOcrPlate(normalizePlate(ocrPlate));
        review.setMatchedPlate(normalizePlate(matchedPlate));
        review.setCandidateList(limit(String.join(",", candidateList != null ? candidateList : List.of()), 500));
        review.setDistance(distance);
        review.setStatus(STATUS_NEEDS_REVIEW);

        PlateCorrectionReviewEntity savedReview = reviewRepository.save(review);
        managerNotificationService.createApartmentNotification(
                apartment,
                "plate_review_required",
                "번호판 확인 필요",
                buildReviewMessage(savedReview),
                REFERENCE_TYPE,
                savedReview.getNo()
        );
        return savedReview;
    }

    public List<PlateCorrectionReviewDto> findPendingReviews(Integer managerNo) {
        ApartmentManagerEntity manager = findManager(managerNo);
        Integer apartmentNo = getApartmentNo(manager);
        return reviewRepository.findByApartment_NoAndStatusOrderByCreatedAtDesc(apartmentNo, STATUS_NEEDS_REVIEW)
                .stream()
                .map(PlateCorrectionReviewDto::from)
                .toList();
    }

    @Transactional
    public Map<String, Object> confirmReview(Integer managerNo, Integer reviewId, String plate) {
        ApartmentManagerEntity manager = findManager(managerNo);
        PlateCorrectionReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "번호판 검토 기록을 찾을 수 없습니다."));
        validateSameApartment(manager, review);

        String normalizedPlate = normalizePlate(plate);
        if (normalizedPlate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plate는 필수입니다.");
        }

        ParkingHistoryEntity history = review.getParkingHistory();
        if (history == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "연결된 주차 이력이 없습니다.");
        }

        ResidentVehicleEntity residentVehicle = findResidentVehicle(normalizedPlate);
        RegisteredCarEntity visitorVehicle = findVisitorVehicle(normalizedPlate);
        history.setPlate(normalizedPlate);
        history.setResidentVehicle(residentVehicle);
        history.setVisitorVehicle(visitorVehicle);

        ParkingZoneEntity zone = history.getParkingZone();
        if (zone != null) {
            zone.setCurrentCarNumber(normalizedPlate);
            zone.setStatusChangeReason("관리자 번호판 보정 확정");
        }

        ParkingZoneEntity linkedZone = findLinkedZone(history);
        if (linkedZone != null) {
            linkedZone.setCurrentCarNumber(normalizedPlate);
            linkedZone.setStatusChangeReason("관리자 번호판 보정 확정");
        }

        review.setSelectedPlate(normalizedPlate);
        review.setStatus(STATUS_CONFIRMED);
        review.setResolvedAt(LocalDateTime.now());
        managerNotificationService.markReferenceAsRead(review.getApartment(), REFERENCE_TYPE, review.getNo());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "ok");
        response.put("review_id", review.getNo());
        response.put("history_id", history.getId());
        response.put("zone", history.getZoneSnapshot());
        response.put("plate", normalizedPlate);
        return response;
    }

    private ApartmentManagerEntity findManager(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));
    }

    private Integer getApartmentNo(ApartmentManagerEntity manager) {
        ApartmentEntity apartment = manager != null ? manager.getApartment() : null;
        if (apartment == null || apartment.getNo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 아파트 정보가 없습니다.");
        }
        return apartment.getNo();
    }

    private void validateSameApartment(ApartmentManagerEntity manager, PlateCorrectionReviewEntity review) {
        Integer managerApartmentNo = getApartmentNo(manager);
        Integer reviewApartmentNo = review.getApartment() != null ? review.getApartment().getNo() : null;
        if (!managerApartmentNo.equals(reviewApartmentNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 아파트의 번호판 검토 기록은 처리할 수 없습니다.");
        }
    }

    private ResidentVehicleEntity findResidentVehicle(String plate) {
        return residentVehicleRepository.findByNumber(plate).orElse(null);
    }

    private RegisteredCarEntity findVisitorVehicle(String plate) {
        return registeredCarRepository.findFirstByNumberAndParkedAtIsNull(plate).orElse(null);
    }

    private ParkingZoneEntity findLinkedZone(ParkingHistoryEntity history) {
        String linkedZone = history.getLinkedZone();
        if (linkedZone == null || linkedZone.isBlank()) {
            return null;
        }
        return parkingZoneRepository.findByAreaNumber(linkedZone.trim()).orElse(null);
    }

    private String buildReviewMessage(PlateCorrectionReviewEntity review) {
        String candidates = review.getCandidateList() == null || review.getCandidateList().isBlank()
                ? "후보 없음"
                : review.getCandidateList();
        return limit(
                review.getZone() + " 구역 OCR " + valueOrDash(review.getOcrPlate())
                        + "가 등록 차량 후보 [" + candidates + "]와 유사합니다. 관리자 확인 후 확정하세요."
                        + (review.getDistance() != null ? " 거리값: " + review.getDistance() + "." : ""),
                255
        );
    }

    private String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return null;
        }
        String compactPlate = plate.replaceAll("\\s+", "");
        return compactPlate.isBlank() ? null : compactPlate;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
