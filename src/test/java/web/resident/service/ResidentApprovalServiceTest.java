package web.resident.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import web.common.mail.GmailMailService;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.repository.ResidentRepository;

class ResidentApprovalServiceTest {

    @Test
    void findSignupRequestsQueriesNewestResidentsFirst() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        GmailMailService gmailMailService = mock(GmailMailService.class);
        when(residentRepository.findByApartment_NoOrderByRegisteredAtDescNoDesc(1)).thenReturn(List.of());

        ResidentApprovalService service = new ResidentApprovalService(
                residentRepository,
                residentVehicleRepository,
                gmailMailService
        );

        service.findSignupRequests(1);

        verify(residentRepository).findByApartment_NoOrderByRegisteredAtDescNoDesc(1);
    }
}
