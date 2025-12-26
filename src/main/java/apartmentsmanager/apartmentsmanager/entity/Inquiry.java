package apartmentsmanager.apartmentsmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a property inquiry from a user
 */
@Entity
@Table(name = "inquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Null if inquiry is from non-registered user
    
    @NotBlank(message = "Името е задължително")
    @Column(nullable = false, length = 255)
    private String name;
    
    @NotBlank(message = "Email адресът е задължителен")
    @Email(message = "Невалиден email адрес")
    @Column(nullable = false, length = 255)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.PENDING;
    
    @Column(name = "agent_response", columnDefinition = "TEXT")
    private String agentResponse;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy; // Agent or admin who responded
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum InquiryStatus {
        PENDING,        // Очаква отговор
        CONTACTED,      // Свързан с клиента
        VIEWING,        // Организиран оглед
        OFFER,          // Направена оферта
        CLOSED,         // Затворено
        SPAM           // Спам
    }
}



