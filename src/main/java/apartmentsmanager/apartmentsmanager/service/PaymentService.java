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
}
