package python.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import python.dto.PlateCorrectionConfirmRequestDto;
import python.dto.PlateCorrectionReviewDto;
import python.service.PlateCorrectionReviewService;

@RestController
@RequiredArgsConstructor
public class PlateCorrectionReviewController {

    private final PlateCorrectionReviewService plateCorrectionReviewService;

    @GetMapping("/api/plate-correction-reviews/pending")
    public ResponseEntity<List<PlateCorrectionReviewDto>> findPendingReviews(
            @AuthenticationPrincipal Map<String, Object> principal
    ) {
        return ResponseEntity.ok(plateCorrectionReviewService.findPendingReviews(getManagerNo(principal)));
    }

    @PatchMapping("/api/plate-correction-reviews/{reviewId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReview(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer reviewId,
            @RequestBody PlateCorrectionConfirmRequestDto requestDto
    ) {
        return ResponseEntity.ok(plateCorrectionReviewService.confirmReview(
                getManagerNo(principal),
                reviewId,
                requestDto != null ? requestDto.getPlate() : null
        ));
    }

    private Integer getManagerNo(Map<String, Object> principal) {
        Object value = principal != null ? principal.get("userNo") : null;
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }
        return null;
    }
}
