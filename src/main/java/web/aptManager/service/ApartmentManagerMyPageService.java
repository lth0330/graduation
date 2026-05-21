package web.aptManager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.dto.ApartmentManagerMyPageDto;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 아파트 관리자 마이페이지 서비스: 관리자 상세 정보를 읽어온다.
public class ApartmentManagerMyPageService {

    private final ApartmentManagerRepository apartmentManagerRepository;

    public ApartmentManagerMyPageDto findMyPage(Integer managerNo) {
        // Read: 관리자 번호로 프로필/아파트 정보를 조회한다.
        ApartmentManagerEntity manager = apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));

        ApartmentEntity apartment = manager.getApartment();

        return ApartmentManagerMyPageDto.builder()
                .managerNo(manager.getNo())
                .loginId(manager.getLoginId())
                .email(manager.getEmail())
                .phone(manager.getPhone())
                .name(manager.getName())
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .apartmentName(apartment != null ? apartment.getName() : null)
                .address(apartment != null ? apartment.getAddress() : manager.getAddress())
                .detailAddress(apartment != null ? apartment.getDetailAddress() : null)
                .apartmentPassword(apartment != null ? apartment.getPassword() : null)
                .build();
    }
}
