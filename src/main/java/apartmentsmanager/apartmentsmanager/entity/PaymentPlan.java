package apartmentsmanager.apartmentsmanager.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Embedded payment plan for apartment
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlan {
    
    // Payment dates
    private LocalDate preliminaryContractDate;
    private LocalDate akt14Date;
    private LocalDate akt15Date;
    private LocalDate akt16Date;
    
    // Payment amounts
    private BigDecimal preliminaryContractAmount;
    private BigDecimal akt14Amount;
    private BigDecimal akt15Amount;
    private BigDecimal akt16Amount;
    
    /**
     * Get all payment dates as list
     */
    public List<LocalDate> getPaymentDates() {
        List<LocalDate> dates = new ArrayList<>();
        if (preliminaryContractDate != null) dates.add(preliminaryContractDate);
        if (akt14Date != null) dates.add(akt14Date);
        if (akt15Date != null) dates.add(akt15Date);
        if (akt16Date != null) dates.add(akt16Date);
        return dates;
    }
    
    /**
     * Get total planned amount
     */
    public BigDecimal getTotalPlannedAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (preliminaryContractAmount != null) total = total.add(preliminaryContractAmount);
        if (akt14Amount != null) total = total.add(akt14Amount);
        if (akt15Amount != null) total = total.add(akt15Amount);
        if (akt16Amount != null) total = total.add(akt16Amount);
        return total;
    }
}
