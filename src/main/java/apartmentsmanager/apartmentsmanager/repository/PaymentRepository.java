package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find all payments for an apartment
    List<Payment> findByApartmentIdOrderByPaymentDateAsc(Long apartmentId);
    
    // Calculate total payments for an apartment
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.apartment.id = :apartmentId")
    BigDecimal calculateTotalByApartment(@Param("apartmentId") Long apartmentId);
    
    // Find payments by date range
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Calculate total bank payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = 'Банка'")
    BigDecimal calculateTotalBankPayments();
    
    // Calculate total cash payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = 'В брой'")
    BigDecimal calculateTotalCashPayments();
}
