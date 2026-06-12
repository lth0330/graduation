package web.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import web.aptManager.entity.ApartmentEntity;
import web.aptManager.entity.ApartmentManagerEntity;
import web.aptManager.repository.ApartmentManagerRepository;
import web.notification.dto.ManagerNotificationDto;
import web.notification.entity.ManagerNotificationEntity;
import web.notification.repository.ManagerNotificationRepository;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingHistoryRepository;

class ManagerNotificationServiceTest {

    @Test
    void createApartmentNotificationReusesUnreadNotificationForSameReference() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ManagerNotificationEntity existingNotification = ManagerNotificationEntity.builder()
                .no(20)
                .apartment(apartment)
                .type("ocr_error")
                .title("번호판 인식 실패")
                .message("기존 메시지")
                .referenceType("parking_history")
                .referenceId(11)
                .read(false)
                .build();

        when(managerNotificationRepository.findByApartment_NoAndTypeAndReferenceTypeAndReferenceIdAndReadFalse(
                1,
                "ocr_error",
                "parking_history",
                11
        )).thenReturn(List.of(existingNotification));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        ManagerNotificationEntity result = service.createApartmentNotification(
                apartment,
                "ocr_error",
                "번호판 인식 실패",
                "A1 구역 번호판 인식 실패. 관리자 확인 필요.",
                "parking_history",
                11
        );

        assertThat(result).isSameAs(existingNotification);
        assertThat(existingNotification.getMessage()).isEqualTo("A1 구역 번호판 인식 실패. 관리자 확인 필요.");
        verify(managerNotificationRepository).findByApartment_NoAndTypeAndReferenceTypeAndReferenceIdAndReadFalse(
                1,
                "ocr_error",
                "parking_history",
                11
        );
    }

    @Test
    void createApartmentNotificationReusesRecentNotificationEvenIfAlreadyRead() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ManagerNotificationEntity recentNotification = ManagerNotificationEntity.builder()
                .no(21)
                .apartment(apartment)
                .type("ocr_error")
                .title("번호판 인식 실패")
                .message("읽은 기존 메시지")
                .referenceType("parking_history")
                .referenceId(11)
                .read(true)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(managerNotificationRepository
                .findFirstByApartment_NoAndTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterOrderByCreatedAtDesc(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                )).thenReturn(Optional.of(recentNotification));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        ManagerNotificationEntity result = service.createApartmentNotification(
                apartment,
                "ocr_error",
                "번호판 인식 실패",
                "A1 구역 번호판 인식 실패. 관리자 확인 필요.",
                "parking_history",
                11
        );

        assertThat(result).isSameAs(recentNotification);
        assertThat(recentNotification.getMessage()).isEqualTo("A1 구역 번호판 인식 실패. 관리자 확인 필요.");
        verify(managerNotificationRepository, never()).save(any());
    }

    @Test
    void findMyNotificationIncludesReferencedParkingHistory() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(3).apartment(apartment).build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(5)
                .parkingLot(parkingLot)
                .areaNumber("A1")
                .build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("A1")
                .plate("UNKNOWN")
                .entryTime(LocalDateTime.of(2026, 6, 8, 10, 30))
                .status("PARKED")
                .parkType("normal")
                .imagePath("C:\\snapshots\\A1.jpg")
                .build();
        ManagerNotificationEntity notification = ManagerNotificationEntity.builder()
                .no(20)
                .apartment(apartment)
                .type("ocr_error")
                .title("번호판 인식 실패")
                .message("A1 구역에서 번호판을 인식하지 못했습니다.")
                .referenceType("parking_history")
                .referenceId(11)
                .read(false)
                .createdAt(LocalDateTime.of(2026, 6, 8, 10, 31))
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(managerNotificationRepository.findById(20)).thenReturn(Optional.of(notification));
        when(parkingHistoryRepository.findById(11)).thenReturn(Optional.of(history));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        ManagerNotificationDto result = service.findMyNotification(Map.of("userNo", 7), 20);

        assertThat(result.getNotificationNo()).isEqualTo(20);
        assertThat(result.getParkingHistory()).isNotNull();
        assertThat(result.getParkingHistory().getHistoryId()).isEqualTo(11);
        assertThat(result.getParkingHistory().getZone()).isEqualTo("A1");
        assertThat(result.getParkingHistory().getPlate()).isEqualTo("UNKNOWN");
        assertThat(result.getParkingHistory().getImagePath()).isEqualTo("C:\\snapshots\\A1.jpg");
    }

    @Test
    void findMyNotificationDoesNotExposeParkingHistoryFromDifferentApartment() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity managerApartment = ApartmentEntity.builder().no(1).name("관리자아파트").build();
        ApartmentEntity otherApartment = ApartmentEntity.builder().no(2).name("다른아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(managerApartment).build();
        ParkingLotEntity parkingLot = ParkingLotEntity.builder().no(3).apartment(otherApartment).build();
        ParkingZoneEntity zone = ParkingZoneEntity.builder()
                .no(5)
                .parkingLot(parkingLot)
                .areaNumber("B1")
                .build();
        ParkingHistoryEntity history = ParkingHistoryEntity.builder()
                .id(11)
                .parkingZone(zone)
                .zoneSnapshot("B1")
                .plate("UNKNOWN")
                .entryTime(LocalDateTime.of(2026, 6, 8, 10, 30))
                .status("PARKED")
                .build();
        ManagerNotificationEntity notification = ManagerNotificationEntity.builder()
                .no(20)
                .apartment(managerApartment)
                .type("ocr_error")
                .title("번호판 인식 실패")
                .message("A1 구역에서 번호판을 인식하지 못했습니다.")
                .referenceType("parking_history")
                .referenceId(11)
                .read(false)
                .createdAt(LocalDateTime.of(2026, 6, 8, 10, 31))
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(managerNotificationRepository.findById(20)).thenReturn(Optional.of(notification));
        when(parkingHistoryRepository.findById(11)).thenReturn(Optional.of(history));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        ManagerNotificationDto result = service.findMyNotification(Map.of("userNo", 7), 20);

        assertThat(result.getParkingHistory()).isNull();
    }

    @Test
    void markAllAsReadMarksOnlyVisibleNotificationsForManagersApartment() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        ManagerNotificationEntity unreadNotification = ManagerNotificationEntity.builder()
                .no(20)
                .apartment(apartment)
                .read(false)
                .build();
        ManagerNotificationEntity readNotification = ManagerNotificationEntity.builder()
                .no(21)
                .apartment(apartment)
                .read(true)
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(managerNotificationRepository.findVisibleToManager(1, 7))
                .thenReturn(List.of(unreadNotification, readNotification));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        Map<String, Object> result = service.markAllAsRead(Map.of("userNo", 7));

        assertThat(unreadNotification.getRead()).isTrue();
        assertThat(readNotification.getRead()).isTrue();
        assertThat(result).containsEntry("updated_count", 1);
    }

    @Test
    void deleteMyNotificationDeletesNotificationFromSameApartment() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        ManagerNotificationEntity notification = ManagerNotificationEntity.builder()
                .no(20)
                .apartment(apartment)
                .read(false)
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(managerNotificationRepository.findById(20)).thenReturn(Optional.of(notification));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        Map<String, Object> result = service.deleteMyNotification(Map.of("userNo", 7), 20);

        verify(managerNotificationRepository).delete(notification);
        assertThat(result).containsEntry("deleted_count", 1);
    }

    @Test
    void deleteAllMyNotificationsDeletesOnlyVisibleNotificationsForManagersApartment() {
        ManagerNotificationRepository managerNotificationRepository = mock(ManagerNotificationRepository.class);
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        ParkingHistoryRepository parkingHistoryRepository = mock(ParkingHistoryRepository.class);
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("테스트아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder().no(7).apartment(apartment).build();
        ManagerNotificationEntity firstNotification = ManagerNotificationEntity.builder()
                .no(20)
                .apartment(apartment)
                .read(false)
                .build();
        ManagerNotificationEntity secondNotification = ManagerNotificationEntity.builder()
                .no(21)
                .apartment(apartment)
                .read(true)
                .build();

        when(apartmentManagerRepository.findById(7)).thenReturn(Optional.of(manager));
        when(managerNotificationRepository.findVisibleToManager(1, 7))
                .thenReturn(List.of(firstNotification, secondNotification));

        ManagerNotificationService service = new ManagerNotificationService(
                managerNotificationRepository,
                apartmentManagerRepository,
                parkingHistoryRepository
        );

        Map<String, Object> result = service.deleteAllMyNotifications(Map.of("userNo", 7));

        verify(managerNotificationRepository).deleteAll(List.of(firstNotification, secondNotification));
        assertThat(result).containsEntry("deleted_count", 2);
    }
}
