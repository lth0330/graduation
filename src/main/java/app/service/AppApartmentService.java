package app.service;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.aptManager.repository.ApartmentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 앱 아파트 조회 서비스: 회원가입에 필요한 아파트 목록을 DB에서 읽어온다.
public class AppApartmentService {

    private final ApartmentRepository apartmentRepository;

    public Map<String, Object> findApartments() {
        // Read: apartments 테이블의 기본 식별값과 이름만 앱 응답 형식으로 변환한다.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("apartments", apartmentRepository.findAll()
                .stream()
                .map(apartment -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("a_no", apartment.getNo());
                    item.put("a_name", apartment.getName());
                    return item;
                })
                .toList());
        return response;
    }
}
