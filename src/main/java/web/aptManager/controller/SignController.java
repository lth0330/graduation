package web.aptManager.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.aptManager.dto.SignDto;
import web.aptManager.service.SignService;

@RestController
@RequestMapping("/api/apartment-managers")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @PostMapping
    public ResponseEntity<SignDto> signup(@RequestBody SignDto signDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signService.signup(signDto));
    }

    @GetMapping
    public ResponseEntity<List<SignDto>> findAll(@RequestParam(required = false) Integer apartmentNo) {
        if (apartmentNo != null) {
            return ResponseEntity.ok(signService.findByApartmentNo(apartmentNo));
        }
        return ResponseEntity.ok(signService.findAll());
    }

    @GetMapping("/{managerNo}")
    public ResponseEntity<SignDto> findByNo(@PathVariable Integer managerNo) {
        return ResponseEntity.ok(signService.findByNo(managerNo));
    }

    @PutMapping("/{managerNo}")
    public ResponseEntity<SignDto> update(@PathVariable Integer managerNo, @RequestBody SignDto signDto) {
        return ResponseEntity.ok(signService.update(managerNo, signDto));
    }

    @DeleteMapping("/{managerNo}")
    public ResponseEntity<Void> delete(@PathVariable Integer managerNo) {
        signService.delete(managerNo);
        return ResponseEntity.noContent().build();
    }
}
