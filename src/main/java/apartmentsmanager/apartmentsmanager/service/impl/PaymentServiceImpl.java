package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Payment;
import apartmentsmanager.apartmentsmanager.repository.ApartmentRepository;
import apartmentsmanager.apartmentsmanager.repository.PaymentRepository;
import apartmentsmanager.apartmentsmanager.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ApartmentRepository apartmentRepository;
    
    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, 
                             ApartmentRepository apartmentRepository) {
        this.paymentRepository = paymentRepository;
        this.apartmentRepository = apartmentRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByApartmentId(Long apartmentId) {
        return paymentRepository.findByApartmentIdOrderByPaymentDateAsc(apartmentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    @Override
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidForApartment(Long apartmentId) {
        BigDecimal total = paymentRepository.calculateTotalByApartment(apartmentId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validatePaymentAmount(Long apartmentId, BigDecimal amount) {
        Optional<Apartment> apartmentOpt = apartmentRepository.findById(apartmentId);
        if (apartmentOpt.isPresent()) {
            Apartment apartment = apartmentOpt.get();
            BigDecimal totalPaid = getTotalPaidForApartment(apartmentId);
            BigDecimal remaining = apartment.getTotalPrice().subtract(totalPaid);
            return amount.compareTo(remaining) <= 0;
        }
        return false;
    }
}
