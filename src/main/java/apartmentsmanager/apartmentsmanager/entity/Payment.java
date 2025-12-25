package apartmentsmanager.apartmentsmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a payment for an apartment
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Сумата е задължителна")
    @DecimalMin(value = "0.01", message = "Сумата трябва да е по-голяма от 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // Сума на плащането
    
    @NotNull(message = "Датата на плащане е задължителна")
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate; // Дата на плащане
    
    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber; // Номер на фактура
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Метод: "Банка" или "В брой"
    
    @Column(name = "payment_stage", length = 100)
    private String paymentStage; // Етап: "Предварителен договор", "Акт 14", "Акт 15", "Акт 16"
    
    @Column(name = "is_deposit", nullable = false)
    private Boolean isDeposit = false; // Дали е капаро
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Many-to-One relationship with Apartment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (amount != null) {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (amount != null) {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
