package apartmentsmanager.apartmentsmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a basement
 */
@Entity
@Table(name = "basements", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"building_id", "basement_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;
    
    @NotBlank(message = "Номерът на мазето е задължително")
    @Column(name = "basement_number", nullable = false, length = 50)
    private String basementNumber;
    
    @NotNull(message = "Площта е задължителна")
    @DecimalMin(value = "0.01", message = "Площта трябва да е по-голяма от 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal area; // Площ в кв.м
    
    @NotNull(message = "Цената на кв.м е задължителна")
    @DecimalMin(value = "0.01", message = "Цената трябва да е по-голяма от 0")
    @Column(name = "price_per_m2", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerM2; // Цена на кв.м
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice; // Обща цена (изчислява се автоматично)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    
    @Column(name = "contract_date")
    private LocalDate contractDate; // Дата на договор
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_sold", nullable = false)
    private Boolean isSold = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    /**
     * Calculate total price from area * pricePerM2
     */
    private void calculateTotalPrice() {
        if (area != null && pricePerM2 != null) {
            totalPrice = area.multiply(pricePerM2).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Get full basement identifier
     */
    public String getFullIdentifier() {
        return building != null ? building.getName() + " - Мазе " + basementNumber : "Мазе " + basementNumber;
    }
}

