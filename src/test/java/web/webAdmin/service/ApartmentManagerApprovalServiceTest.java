package web.webAdmin.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import web.aptManager.repository.ApartmentManagerRepository;
import web.common.mail.GmailMailService;

class ApartmentManagerApprovalServiceTest {

    @Test
    void findSignupRequestsQueriesNewestRequestsFirst() {
        ApartmentManagerRepository apartmentManagerRepository = mock(ApartmentManagerRepository.class);
        GmailMailService gmailMailService = mock(GmailMailService.class);
        when(apartmentManagerRepository.findAllByOrderByRequestedAtDescNoDesc()).thenReturn(List.of());

        ApartmentManagerApprovalService service = new ApartmentManagerApprovalService(
                apartmentManagerRepository,
                gmailMailService
        );

        service.findSignupRequests();

        verify(apartmentManagerRepository).findAllByOrderByRequestedAtDescNoDesc();
    }
}
