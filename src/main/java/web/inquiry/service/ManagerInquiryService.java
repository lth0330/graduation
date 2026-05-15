package web.inquiry.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.inquiry.dto.ManagerInquiryAnswerRequestDto;
import web.inquiry.dto.ManagerInquiryCreateRequestDto;
import web.inquiry.dto.ManagerInquiryDto;
import web.inquiry.entity.ManagerInquiryEntity;
import web.inquiry.repository.ManagerInquiryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerInquiryService {

    private final ManagerInquiryRepository managerInquiryRepository;
    private final ApartmentManagerRepository apartmentManagerRepository;

    @Transactional
    public ManagerInquiryDto create(Map<String, Object> principal, ManagerInquiryCreateRequestDto requestDto) {
        validateCreateRequest(requestDto);

        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        ManagerInquiryEntity inquiry = ManagerInquiryEntity.builder()
                .manager(manager)
                .title(requestDto.getTitle().trim())
                .category(requestDto.getCategory().trim())
                .content(requestDto.getContent().trim())
                .status("pending")
                .build();

        return toDto(managerInquiryRepository.save(inquiry));
    }

    public List<ManagerInquiryDto> findMine(Map<String, Object> principal) {
        Integer managerNo = getInteger(principal, "userNo");
        return managerInquiryRepository.findByManager_NoOrderByCreatedAtDesc(managerNo)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ManagerInquiryDto> findAll() {
        return managerInquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ManagerInquiryDto findByNo(Integer inquiryNo) {
        return toDto(findEntity(inquiryNo));
    }

    @Transactional
    public ManagerInquiryDto answer(Integer inquiryNo, ManagerInquiryAnswerRequestDto requestDto) {
        if (requestDto == null || isBlank(requestDto.getAnswer())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변 내용을 입력해주세요.");
        }

        ManagerInquiryEntity inquiry = findEntity(inquiryNo);
        inquiry.setAnswer(requestDto.getAnswer().trim());
        inquiry.setStatus("answered");
        inquiry.setAnsweredAt(LocalDateTime.now());
        return toDto(inquiry);
    }

    private void validateCreateRequest(ManagerInquiryCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 정보를 입력해주세요.");
        }
        if (isBlank(requestDto.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 제목은 필수입니다.");
        }
        if (isBlank(requestDto.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 카테고리는 필수입니다.");
        }
        if (isBlank(requestDto.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "문의 내용은 필수입니다.");
        }
    }

    private ManagerInquiryEntity findEntity(Integer inquiryNo) {
        return managerInquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 문의입니다."));
    }

    private ApartmentManagerEntity findManager(Integer managerNo) {
        return apartmentManagerRepository.findById(managerNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아파트 관리자입니다."));
    }

    private ManagerInquiryDto toDto(ManagerInquiryEntity inquiry) {
        ApartmentManagerEntity manager = inquiry.getManager();
        ApartmentEntity apartment = manager != null ? manager.getApartment() : null;

        return ManagerInquiryDto.builder()
                .inquiryNo(inquiry.getNo())
                .managerNo(manager != null ? manager.getNo() : null)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .apartmentName(apartment != null ? apartment.getName() : null)
                .writer(manager != null ? manager.getName() : null)
                .title(inquiry.getTitle())
                .category(inquiry.getCategory())
                .content(inquiry.getContent())
                .status(inquiry.getStatus())
                .answer(inquiry.getAnswer())
                .createdAt(inquiry.getCreatedAt())
                .answeredAt(inquiry.getAnsweredAt())
                .build();
    }

    private Integer getInteger(Map<String, Object> principal, String key) {
        Object value = principal != null ? principal.get(key) : null;

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
