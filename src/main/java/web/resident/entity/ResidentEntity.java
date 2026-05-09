package web.resident.entity;

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
import web.aptManager.entity.ApartmentEntity;
import web.common.type.ApprovalStatus;
import web.resident.dto.ResidentDto;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_no")
    private Integer no;

    @Column(name = "u_id", length = 20, nullable = false, unique = true)
    private String loginId;

    @Column(name = "u_pwd", length = 20, nullable = false)
    private String password;

    @Column(name = "u_name", length = 30, nullable = false)
    private String name;

    @Column(name = "u_email", nullable = false)
    private String email;

    @Column(name = "u_phone", length = 20)
    private String phone;

    @Column(name = "p_date")
    private LocalDateTime registeredAt;

    @Column(name = "u_dong", length = 20, nullable = false)
    private String dong;

    @Column(name = "u_ho", length = 20, nullable = false)
    private String ho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_no")
    private ApartmentEntity apartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20, nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }
    }

    public ResidentDto toDTO() {
        return ResidentDto.builder()
                .residentNo(no)
                .loginId(loginId)
                .name(name)
                .email(email)
                .phone(phone)
                .registeredAt(registeredAt)
                .dong(dong)
                .ho(ho)
                .apartmentNo(apartment != null ? apartment.getNo() : null)
                .approvalStatus(approvalStatus)
                .rejectReason(rejectReason)
                .build();
    }
}
