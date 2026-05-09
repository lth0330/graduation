package web.aptManager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.aptManager.dto.ApartmentManagerMyPageDto;
import web.aptManager.service.ApartmentManagerMyPageService;

@RestController
@RequestMapping("/api/apartment-managers")
@RequiredArgsConstructor
public class ApartmentManagerMyPageController {

    private final ApartmentManagerMyPageService apartmentManagerMyPageService;

    @GetMapping("/{managerNo}/my-page")
    public ResponseEntity<ApartmentManagerMyPageDto> findMyPage(@PathVariable Integer managerNo) {
        return ResponseEntity.ok(apartmentManagerMyPageService.findMyPage(managerNo));
    }
}
