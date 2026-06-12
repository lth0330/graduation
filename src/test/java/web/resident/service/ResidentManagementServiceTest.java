package web.resident.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import web.aptManager.repository.ApartmentRepository;
import web.common.type.ApprovalStatus;
import web.parking.repository.ResidentVehicleRepository;
import web.resident.repository.ResidentRepository;

class ResidentManagementServiceTest {

    @Test
    void findApprovedResidentsQueriesNewestResidentsFirst() {
        ResidentRepository residentRepository = mock(ResidentRepository.class);
        ResidentVehicleRepository residentVehicleRepository = mock(ResidentVehicleRepository.class);
        ApartmentRepository apartmentRepository = mock(ApartmentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(residentRepository.findByApartment_NoAndApprovalStatusOrderByRegisteredAtDescNoDesc(
                1,
                ApprovalStatus.APPROVED
        )).thenReturn(List.of());

        ResidentManagementService service = new ResidentManagementService(
                residentRepository,
                residentVehicleRepository,
                apartmentRepository,
                passwordEncoder
        );

        service.findApprovedResidents(1);

        verify(residentRepository).findByApartment_NoAndApprovalStatusOrderByRegisteredAtDescNoDesc(
                1,
                ApprovalStatus.APPROVED
        );
    }
}
