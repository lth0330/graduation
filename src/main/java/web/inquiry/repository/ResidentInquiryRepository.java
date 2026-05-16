package web.inquiry.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import web.inquiry.entity.ResidentInquiryEntity;

public interface ResidentInquiryRepository extends JpaRepository<ResidentInquiryEntity, Integer> {

    List<ResidentInquiryEntity> findByResident_Apartment_NoOrderByCreatedAtDesc(Integer apartmentNo);

    List<ResidentInquiryEntity> findByResident_NoOrderByCreatedAtDesc(Integer residentNo);

    long countByResident_Apartment_NoAndStatus(Integer apartmentNo, String status);
}
