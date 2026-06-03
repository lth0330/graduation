package web.inquiry.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import web.inquiry.entity.ResidentInquiryEntity;

public interface ResidentInquiryRepository extends JpaRepository<ResidentInquiryEntity, Integer> {

    List<ResidentInquiryEntity> findByResident_Apartment_NoOrderByCreatedAtDesc(Integer apartmentNo);

    List<ResidentInquiryEntity> findByResident_NoOrderByCreatedAtDesc(Integer residentNo);

    List<ResidentInquiryEntity> findByVehicle_No(Integer vehicleNo);

    long countByResident_Apartment_NoAndStatus(Integer apartmentNo, String status);
}
