package web.notification.service;

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
import web.notification.dto.ManagerNotificationDto;
import web.notification.entity.ManagerNotificationEntity;
import web.notification.repository.ManagerNotificationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerNotificationService {

    private final ManagerNotificationRepository managerNotificationRepository;
    private final ApartmentManagerRepository apartmentManagerRepository;

    @Transactional
    public ManagerNotificationEntity createApartmentNotification(
            ApartmentEntity apartment,
            String type,
            String title,
            String message,
            String referenceType,
            Integer referenceId
    ) {
        if (apartment == null || apartment.getNo() == null) {
            return null;
        }

        return managerNotificationRepository.save(ManagerNotificationEntity.builder()
                .apartment(apartment)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .build());
    }

    public List<ManagerNotificationDto> findMyNotifications(Map<String, Object> principal) {
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        Integer apartmentNo = getApartmentNo(manager);

        return managerNotificationRepository.findVisibleToManager(apartmentNo, manager.getNo())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ManagerNotificationDto markAsRead(Map<String, Object> principal, Integer notificationNo) {
        ApartmentManagerEntity manager = findManager(getInteger(principal, "userNo"));
        ManagerNotificationEntity notification = managerNotificationRepository.findById(notificationNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 관리자 알림입니다."));

        Integer notificationApartmentNo = notification.getApartment() != null ? notification.getApartment().getNo() : null;
        if (!getApartmentNo(manager).equals(notificationApartmentNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 아파트의 알림은 처리할 수 없습니다.");
        }

        notification.setRead(true);
        return toDto(notification);
    }

    @Transactional
    public void markReferenceAsRead(ApartmentEntity apartment, String referenceType, Integer referenceId) {
        if (apartment == null || apartment.getNo() == null || referenceType == null || referenceId == null) {
            return;
        }

        managerNotificationRepository.findByApartment_NoAndReferenceTypeAndReferenceIdAndReadFalse(
                apartment.getNo(),
                referenceType,
                referenceId
        ).forEach(notification -> notification.setRead(true));
    }

    private ManagerNotificationDto toDto(ManagerNotificationEntity notification) {
        ApartmentManagerEntity manager = notification.getManager();
        ApartmentEntity apartment = notification.getApartment();

        return ManagerNotificationDto.builder()
                .notificationNo(notification.getNo())
                .managerNo(manager != null ? manager.getNo() : null)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .notificationType(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
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
}
