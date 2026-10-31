package web.aptManager.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import web.aptManager.dto.SignDto;

class ApartmentManagerEntityTest {

    @Test
    void toDtoIncludesApartmentNameForWebAdminManagerList() {
        ApartmentEntity apartment = ApartmentEntity.builder().no(1).name("성결아파트").build();
        ApartmentManagerEntity manager = ApartmentManagerEntity.builder()
                .no(1)
                .loginId("aptadmin")
                .apartment(apartment)
                .build();

        SignDto dto = manager.toDTO();

        assertThat(dto.getApartmentNo()).isEqualTo(1);
        assertThat(dto.getApartmentName()).isEqualTo("성결아파트");
    }
}
