package web.inquiry.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import web.inquiry.entity.ManagerInquiryEntity;

public interface ManagerInquiryRepository extends JpaRepository<ManagerInquiryEntity, Integer> {

    List<ManagerInquiryEntity> findByManager_NoOrderByCreatedAtDesc(Integer managerNo);

    List<ManagerInquiryEntity> findAllByOrderByCreatedAtDesc();
}
