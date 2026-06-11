package python.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import python.entity.PlateCorrectionReviewEntity;
import web.parking.dto.ParkingHistoryDto;
import web.parking.entity.ParkingHistoryEntity;

@Getter
@Builder
public class PlateCorrectionReviewDto {

    private Integer reviewId;
    private Integer apartmentNo;
    private Integer historyId;
    private String zone;
    private String ocrPlate;
    private String matchedPlate;
    private String selectedPlate;
    private List<String> candidateList;
    private Integer distance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private ParkingHistoryDto parkingHistory;

    public static PlateCorrectionReviewDto from(PlateCorrectionReviewEntity review) {
        ParkingHistoryEntity history = review.getParkingHistory();
        return PlateCorrectionReviewDto.builder()
                .reviewId(review.getNo())
                .apartmentNo(review.getApartment() != null ? review.getApartment().getNo() : null)
                .historyId(history != null ? history.getId() : null)
                .zone(review.getZone())
                .ocrPlate(review.getOcrPlate())
                .matchedPlate(review.getMatchedPlate())
                .selectedPlate(review.getSelectedPlate())
                .candidateList(splitCandidates(review.getCandidateList()))
                .distance(review.getDistance())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .resolvedAt(review.getResolvedAt())
                .parkingHistory(history != null ? ParkingHistoryDto.from(history) : null)
                .build();
    }

    private static List<String> splitCandidates(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(candidate -> !candidate.isBlank())
                .toList();
    }
}
