package web.parking.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import web.inquiry.repository.ResidentInquiryRepository;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.repository.ResidentRepository;

class VehicleManagementServiceTest {

    @Test
    void findVehiclesQueriesNewestVehiclesFirst() {
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentInquiryRepository residentInquiryRepository = mock(ResidentInquiryRepository.class);
        when(residentVehicleRepository.findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(1))
                .thenReturn(List.of());

        VehicleManagementService service = new VehicleManagementService(
                residentVehicleRepository,
                residentRepository,
                residentInquiryRepository
        );

        service.findVehicles(1);

        verify(residentVehicleRepository).findByResident_Apartment_NoOrderByRegisteredAtDescNoDesc(1);
    }
}
