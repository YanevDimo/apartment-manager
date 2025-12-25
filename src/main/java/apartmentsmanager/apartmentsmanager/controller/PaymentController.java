package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Payment;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final ApartmentService apartmentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService, ApartmentService apartmentService) {
        this.paymentService = paymentService;
        this.apartmentService = apartmentService;
    }
    
    @GetMapping
    public String paymentsPage(Model model) {
        return "payments";
    }
    
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllPaymentsApi() {
        // Get all payments from all apartments
        List<Apartment> apartments = apartmentService.getAllSoldApartments();
        List<Map<String, Object>> paymentData = apartments.stream()
                .flatMap(apt -> paymentService.getPaymentsByApartmentId(apt.getId()).stream())
                .map(payment -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", payment.getId());
                    data.put("apartmentId", payment.getApartment().getId());
                    data.put("apartment", payment.getApartment().getFullIdentifier());
                    data.put("amount", payment.getAmount());
                    data.put("paymentDate", payment.getPaymentDate().toString());
                    data.put("invoiceNumber", payment.getInvoiceNumber());
                    data.put("paymentMethod", payment.getPaymentMethod());
                    data.put("paymentStage", payment.getPaymentStage());
                    data.put("isDeposit", payment.getIsDeposit());
                    data.put("notes", payment.getNotes());
                    return data;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("payments", paymentData);
        response.put("total", paymentData.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/by-apartment/{apartmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentsByApartmentApi(@PathVariable Long apartmentId) {
        List<Payment> payments = paymentService.getPaymentsByApartmentId(apartmentId);
        
        List<Map<String, Object>> paymentData = payments.stream().map(payment -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", payment.getId());
            data.put("amount", payment.getAmount());
            data.put("paymentDate", payment.getPaymentDate().toString());
            data.put("invoiceNumber", payment.getInvoiceNumber());
            data.put("paymentMethod", payment.getPaymentMethod());
            data.put("paymentStage", payment.getPaymentStage());
            data.put("isDeposit", payment.getIsDeposit());
            data.put("notes", payment.getNotes());
            return data;
        }).collect(Collectors.toList());
        
        // Get apartment info
        Apartment apartment = apartmentService.getApartmentById(apartmentId).orElse(null);
        BigDecimal totalPaid = paymentService.getTotalPaidForApartment(apartmentId);
        BigDecimal remaining = apartment != null && apartment.getTotalPrice() != null 
                ? apartment.getTotalPrice().subtract(totalPaid) 
                : BigDecimal.ZERO;
        
        Map<String, Object> response = new HashMap<>();
        response.put("payments", paymentData);
        response.put("total", payments.size());
        response.put("totalPaid", totalPaid);
        response.put("remainingPayment", remaining);
        response.put("apartment", apartment != null ? apartment.getFullIdentifier() : "");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addPayment(@Valid @RequestBody Payment payment,
                                                          BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Грешка при валидация на данните");
            response.put("errors", bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate apartment exists
        if (payment.getApartment() == null || payment.getApartment().getId() == null) {
            response.put("success", false);
            response.put("message", "Апартаментът е задължителен");
            return ResponseEntity.badRequest().body(response);
        }
        
        Apartment apartment = apartmentService.getApartmentById(payment.getApartment().getId())
                .orElse(null);
        
        if (apartment == null) {
            response.put("success", false);
            response.put("message", "Апартаментът не е намерен");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate payment amount
        if (!paymentService.validatePaymentAmount(apartment.getId(), payment.getAmount())) {
            response.put("success", false);
            response.put("message", "Сумата на плащането надвишава остатъка за плащане");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Set apartment reference
        payment.setApartment(apartment);
        
        try {
            Payment saved = paymentService.savePayment(payment);
            response.put("success", true);
            response.put("message", "Плащането е добавено успешно");
            response.put("payment", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при добавяне на плащане: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePayment(@PathVariable Long id,
                                                            @Valid @RequestBody Payment payment,
                                                            BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Грешка при валидация на данните");
            response.put("errors", bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(response);
        }
        
        Payment existingPayment = paymentService.getPaymentById(id).orElse(null);
        if (existingPayment == null) {
            response.put("success", false);
            response.put("message", "Плащането не е намерено");
            return ResponseEntity.notFound().build();
        }
        
        // Validate apartment exists
        if (payment.getApartment() == null || payment.getApartment().getId() == null) {
            response.put("success", false);
            response.put("message", "Апартаментът е задължителен");
            return ResponseEntity.badRequest().body(response);
        }
        
        Apartment apartment = apartmentService.getApartmentById(payment.getApartment().getId())
                .orElse(null);
        
        if (apartment == null) {
            response.put("success", false);
            response.put("message", "Апартаментът не е намерен");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate payment amount (excluding current payment amount)
        BigDecimal currentTotal = paymentService.getTotalPaidForApartment(apartment.getId());
        BigDecimal currentPaymentAmount = existingPayment.getAmount();
        BigDecimal newRemaining = apartment.getTotalPrice()
                .subtract(currentTotal)
                .add(currentPaymentAmount); // Add back the current payment amount
        
        if (payment.getAmount().compareTo(newRemaining) > 0) {
            response.put("success", false);
            response.put("message", "Сумата на плащането надвишава остатъка за плащане");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Update payment
        payment.setId(id);
        payment.setApartment(apartment);
        
        try {
            Payment saved = paymentService.savePayment(payment);
            response.put("success", true);
            response.put("message", "Плащането е обновено успешно");
            response.put("payment", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при обновяване на плащане: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePayment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            paymentService.deletePayment(id);
            response.put("success", true);
            response.put("message", "Плащането е изтрито успешно");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при изтриване: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/apartment/{apartmentId}/total")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTotalPaidForApartment(@PathVariable Long apartmentId) {
        BigDecimal totalPaid = paymentService.getTotalPaidForApartment(apartmentId);
        Apartment apartment = apartmentService.getApartmentById(apartmentId).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("apartmentId", apartmentId);
        response.put("totalPaid", totalPaid);
        
        if (apartment != null && apartment.getTotalPrice() != null) {
            response.put("totalPrice", apartment.getTotalPrice());
            response.put("remainingPayment", apartment.getTotalPrice().subtract(totalPaid));
        }
        
        return ResponseEntity.ok(response);
    }
}


