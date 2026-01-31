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
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an apartment
 */
@Entity
@Table(name = "apartments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"building_id", "apartment_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;
    
    // Keep buildingName for backward compatibility (can be removed later)
    @Column(name = "building_name", length = 255)
    private String buildingName;
    
    @NotBlank(message = "Номерът на апартамента е задължително")
    @Column(name = "apartment_number", nullable = false, length = 50)
    private String apartmentNumber;
    
    @NotNull(message = "Площта е задължителна")
    @DecimalMin(value = "0.01", message = "Площта трябва да е по-голяма от 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal area; // Площ в кв.м
    
    @DecimalMin(value = "0.01", message = "Цената трябва да е по-голяма от 0")
    @Column(name = "price_per_m2", nullable = true, precision = 15, scale = 2)
    private BigDecimal pricePerM2; // Цена на кв.м
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice; // Обща цена (изчислява се автоматично)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    
    @Column(name = "stage", length = 100)
    private String stage; // Етап: Завършен, В процес, Незапочнат, Акт 14, Акт 15, Акт 16
    
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
    
    // One-to-Many relationship with Payments
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();
    
    // Embedded PaymentPlan
    @Embedded
    private PaymentPlan paymentPlan = new PaymentPlan();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Calculate totalPrice only if not already set
        if (totalPrice == null) {
            calculateTotalPrice();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Don't auto-calculate totalPrice if apartment is sold (has client)
        // to preserve the exact purchase price
        if (client == null) {
            calculateTotalPrice();
        }
    }
    
    /**
     * Calculate total price from area * pricePerM2
     * Only used for unsold apartments
     */
    private void calculateTotalPrice() {
        if (area != null && pricePerM2 != null) {
            totalPrice = area.multiply(pricePerM2).setScale(2, RoundingMode.HALF_UP);
        } else if (totalPrice == null) {
            totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Get total paid amount from all payments
     */
    public BigDecimal getTotalPaid() {
        if (payments == null || payments.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate remaining payment amount
     */
    public BigDecimal getRemainingPayment() {
        if (totalPrice == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalPrice.subtract(getTotalPaid()).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if apartment has overdue payments
     */
    public boolean hasOverduePayments() {
        if (paymentPlan == null || payments == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return paymentPlan.getPaymentDates().stream()
                .anyMatch(date -> date.isBefore(today) && !isPaymentReceivedForDate(date));
    }
    
    /**
     * Check if payment is received for a specific date
     */
    private boolean isPaymentReceivedForDate(LocalDate date) {
        return payments.stream()
                .anyMatch(p -> p.getPaymentDate().equals(date) || 
                             (p.getPaymentDate().isBefore(date) && p.getPaymentStage() != null));
    }
    
    /**
     * Get full apartment identifier
     */
    public String getFullIdentifier() {
        if (building != null) {
            return building.getName() + " - " + apartmentNumber;
        }
        return (buildingName != null ? buildingName : "Неизвестна сграда") + " - " + apartmentNumber;
    }
}




