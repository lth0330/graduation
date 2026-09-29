package web.aptManager.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.aptManager.dto.ApartmentManagerMyPageDto;
import web.aptManager.service.ApartmentManagerMyPageService;
import web.common.auth.ManagerAccessGuard;

@RestController
@RequestMapping("/api/apartment-managers")
@RequiredArgsConstructor
// 아파트 관리자 마이페이지 컨트롤러: 관리자 본인 정보를 조회한다.
public class ApartmentManagerMyPageController {

    private final ApartmentManagerMyPageService apartmentManagerMyPageService;

    @GetMapping("/{managerNo}/my-page")
    // Read: 관리자 번호로 마이페이지 정보를 조회한다. 다른 관리자의 번호로는 조회할 수 없다.
    public ResponseEntity<ApartmentManagerMyPageDto> findMyPage(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer managerNo
    ) {
        ManagerAccessGuard.checkSelfOrWebAdmin(principal, managerNo);
        return ResponseEntity.ok(apartmentManagerMyPageService.findMyPage(managerNo));
    }
}
