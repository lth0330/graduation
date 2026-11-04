package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import web.parking.entity.ParkingZoneEntity;
import web.parking.repository.ParkingZoneRepository;

class AppParkingServiceTest {

    private final ParkingZoneRepository repository = mock(ParkingZoneRepository.class);
    private final AppParkingService service = new AppParkingService(repository);

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> zonesFor(ParkingZoneEntity... zones) {
        when(repository.findAllByOrderByNoAsc()).thenReturn(List.of(zones));
        return (List<Map<String, Object>>) service.findParkingZones().get("zones");
    }

    @Test
    void adminChangedZoneWithoutCarNumberSendsBlankSoAppDoesNotTreatItAsMyCar() {
        List<Map<String, Object>> zones = zonesFor(
                ParkingZoneEntity.builder().areaNumber("a-b1-001").status("disabled").build(),
                ParkingZoneEntity.builder().areaNumber("a-b1-002").status("occupied").build()
        );

        assertThat(zones.get(0).get("current_car_number")).isEqualTo("");
        assertThat(zones.get(1).get("current_car_number")).isEqualTo("");
    }

    @Test
    void emptyZoneKeepsNullAndParkedCarKeepsItsNumber() {
        List<Map<String, Object>> zones = zonesFor(
                ParkingZoneEntity.builder().areaNumber("a-b1-003").status("empty").build(),
                ParkingZoneEntity.builder().areaNumber("a-b1-004").status("occupied").currentCarNumber("37나5209").build()
        );

        assertThat(zones.get(0).get("current_car_number")).isNull();
        assertThat(zones.get(1).get("current_car_number")).isEqualTo("37나5209");
    }
}
