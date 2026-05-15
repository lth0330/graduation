package web.inquiry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import web.aptManager.entity.ApartmentManagerEntity;

@Entity
@Table(name = "manager_inquiry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerInquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_no")
    private Integer no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_no", nullable = false)
    private ApartmentManagerEntity manager;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "answer", length = 2000)
    private String answer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = "pending";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
