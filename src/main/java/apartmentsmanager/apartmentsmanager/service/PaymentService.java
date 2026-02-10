package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    
    List<Payment> getPaymentsByApartmentId(Long apartmentId);
    
    Optional<Payment> getPaymentById(Long id);
    
    Payment savePayment(Payment payment);
    
    void deletePayment(Long id);
    
    BigDecimal getTotalPaidForApartment(Long apartmentId);
    
    boolean validatePaymentAmount(Long apartmentId, BigDecimal amount);
    
    /** Total amount paid by bank for the given apartment. */
    BigDecimal getTotalPaidByBankForApartment(Long apartmentId);
    
    /** Whether another payment with method "Банка" is allowed for this apartment (required bank amount not yet reached). */
    boolean canAcceptBankPayment(Long apartmentId);
    
    /** Whether adding a bank payment of the given amount would not exceed the bank limit. */
    boolean validateBankPaymentAmount(Long apartmentId, BigDecimal amount);
    
    /** Remaining amount that can still be paid by bank before reaching the limit (null if no limit). */
    BigDecimal getRemainingBankAmount(Long apartmentId);
    
    /** Whether updating a payment to the given method/amount is allowed (for bank cap). */
    boolean canAcceptBankPaymentForUpdate(Long apartmentId, String currentMethod, BigDecimal currentAmount, String newMethod, BigDecimal newAmount);
}
