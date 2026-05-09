package web.aptManager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.aptManager.dto.SignDto;
import web.common.type.ApprovalStatus;
import web.webAdmin.dto.ApartmentManagerSignupListDto;

@Entity
@Table(name = "apartment_manager")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentManagerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "m_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_no", nullable = false)
    private ApartmentEntity apartment;

    @Column(name = "m_id", length = 20, nullable = false, unique = true)
    private String loginId;

    @Column(name = "m_pwd", nullable = false)
    private String password;

    @Column(name = "m_email", nullable = false)
    private String email;

    @Column(name = "m_phone", length = 20)
    private String phone;

    @Column(name = "m_address", length = 150)
    private String address;

    @Column(name = "m_name", length = 30)
    private String name;

    @Column(name = "picture", nullable = false)
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20, nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    public void prePersist() {
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    public SignDto toDTO() {
        return SignDto.builder()
                .managerNo(no)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .loginId(loginId)
                .email(email)
                .phone(phone)
                .address(address)
                .name(name)
                .picture(picture)
                .approvalStatus(approvalStatus)
                .rejectReason(rejectReason)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }

    public ApartmentManagerSignupListDto toSignupListDTO() {
        return ApartmentManagerSignupListDto.builder()
                .managerNo(no)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .apartmentName(apartment != null ? apartment.getName() : null)
                .loginId(loginId)
                .email(email)
                .phone(phone)
                .address(address)
                .name(name)
                .picture(picture)
                .approvalStatus(approvalStatus)
                .rejectReason(rejectReason)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }
}
