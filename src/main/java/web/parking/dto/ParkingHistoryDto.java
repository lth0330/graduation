package web.parking.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import web.aptManager.entity.ApartmentEntity;
import web.parking.entity.ParkingHistoryEntity;
import web.parking.entity.ParkingLotEntity;
import web.parking.entity.ParkingZoneEntity;

@Getter
@Builder
public class ParkingHistoryDto {

    private Integer historyId;
    private Integer parkingZoneNo;
    private Integer parkingLotNo;
    private Integer apartmentNo;
    private String zone;
    private String plate;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String status;
    private String parkType;
    private String linkedZone;
    private String imagePath;

    public static ParkingHistoryDto from(ParkingHistoryEntity history) {
        ParkingZoneEntity zone = history.getParkingZone();
        ParkingLotEntity parkingLot = zone != null ? zone.getParkingLot() : null;
        ApartmentEntity apartment = parkingLot != null ? parkingLot.getApartment() : null;

        return ParkingHistoryDto.builder()
                .historyId(history.getId())
                .parkingZoneNo(zone != null ? zone.getNo() : null)
                .parkingLotNo(parkingLot != null ? parkingLot.getNo() : null)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .zone(history.getZoneSnapshot())
                .plate(history.getPlate())
                .entryTime(history.getEntryTime())
                .exitTime(history.getExitTime())
                .status(history.getStatus())
                .parkType(history.getParkType())
                .linkedZone(history.getLinkedZone())
                .imagePath(history.getImagePath())
                .build();
    }
}
