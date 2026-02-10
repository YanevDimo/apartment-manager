package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Payment;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
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
    private final BuildingService buildingService;
    
    @Autowired
    public PaymentController(PaymentService paymentService, ApartmentService apartmentService, BuildingService buildingService) {
        this.paymentService = paymentService;
        this.apartmentService = apartmentService;
        this.buildingService = buildingService;
    }
    
    @GetMapping
    public String paymentsPage(Model model) {
        return "payments";
    }
    
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllPaymentsApi() {
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
        // Get payments only for current building
        List<Apartment> apartments = buildingId != null
            ? apartmentService.getAllSoldApartmentsByBuilding(buildingId)
            : List.of();
        List<Map<String, Object>> paymentData = apartments.stream()
                .flatMap(apt -> paymentService.getPaymentsByApartmentId(apt.getId()).stream()
                        .map(payment -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", payment.getId());
                            data.put("apartmentId", apt.getId());
                            data.put("apartment", apt.getApartmentNumber());
                            data.put("buildingId", apt.getBuilding() != null ? apt.getBuilding().getId() : null);
                            data.put("buildingName", apt.getBuilding() != null ? apt.getBuilding().getName() : null);
                            data.put("clientId", apt.getClient() != null ? apt.getClient().getId() : null);
                            data.put("clientName", apt.getClient() != null ? apt.getClient().getName() : null);
                            data.put("amount", payment.getAmount());
                            data.put("paymentDate", payment.getPaymentDate().toString());
                            data.put("invoiceNumber", payment.getInvoiceNumber());
                            data.put("paymentMethod", payment.getPaymentMethod());
                            data.put("paymentStage", payment.getPaymentStage());
                            data.put("isDeposit", payment.getIsDeposit());
                            data.put("notes", payment.getNotes());
                            return data;
                        }))
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
        
        // If method is bank: 1) limit not yet reached, 2) this payment must not exceed the limit
        if (isBankMethod(payment.getPaymentMethod())) {
            if (!paymentService.canAcceptBankPayment(apartment.getId())) {
                response.put("success", false);
                response.put("message", "За този обект лимитът по банка вече е достигнат. Допускат се само плащания в брой.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!paymentService.validateBankPaymentAmount(apartment.getId(), payment.getAmount())) {
                BigDecimal remaining = paymentService.getRemainingBankAmount(apartment.getId());
                String msg = remaining != null
                    ? String.format("Сумата по банка надвишава лимита. Остава по банка: %s €. Намалете сумата или платете в брой.", remaining.setScale(2, java.math.RoundingMode.HALF_UP))
                    : "Сумата по банка надвишава зададения лимит за този обект.";
                response.put("success", false);
                response.put("message", msg);
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        // Set apartment reference
        payment.setApartment(apartment);
        
        try {
            Payment saved = paymentService.savePayment(payment);
            response.put("success", true);
            response.put("message", "Плащането е добавено успешно");
            // Return only basic payment data to avoid LazyInitializationException
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("id", saved.getId());
            paymentData.put("amount", saved.getAmount());
            paymentData.put("paymentDate", saved.getPaymentDate().toString());
            response.put("payment", paymentData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при добавяне на плащане: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/apartment/{apartmentId}/hasDeposit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkDepositForApartment(@PathVariable Long apartmentId) {
        List<Payment> payments = paymentService.getPaymentsByApartmentId(apartmentId);
        boolean hasDeposit = payments.stream()
                .anyMatch(p -> p.getIsDeposit() != null && p.getIsDeposit());
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasDeposit", hasDeposit);
        
        return ResponseEntity.ok(response);
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
        
        // If new method is bank, check that total bank after update does not exceed limit
        if (isBankMethod(payment.getPaymentMethod()) && !paymentService.canAcceptBankPaymentForUpdate(
                apartment.getId(), existingPayment.getPaymentMethod(), existingPayment.getAmount(),
                payment.getPaymentMethod(), payment.getAmount())) {
            response.put("success", false);
            response.put("message", "Сумата по банка надвишава лимита за този обект. Намалете сумата или изберете плащане в брой.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Update payment
        payment.setId(id);
        payment.setApartment(apartment);
        
        try {
            Payment saved = paymentService.savePayment(payment);
            response.put("success", true);
            response.put("message", "Плащането е обновено успешно");
            // Return only basic payment data to avoid LazyInitializationException
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("id", saved.getId());
            paymentData.put("amount", saved.getAmount());
            paymentData.put("paymentDate", saved.getPaymentDate().toString());
            response.put("payment", paymentData);
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
    
    private static boolean isBankMethod(String method) {
        return method != null && ("Банка".equals(method) || "Bank Transfer".equals(method) || "Bank".equals(method));
    }
    
    @GetMapping("/api/apartment/{apartmentId}/allowed-payment-methods")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllowedPaymentMethods(@PathVariable Long apartmentId) {
        Apartment apartment = apartmentService.getApartmentById(apartmentId).orElse(null);
        if (apartment == null) {
            return ResponseEntity.notFound().build();
        }
        BigDecimal requiredBank = apartment.getRequiredBankAmount();
        BigDecimal paidByBank = paymentService.getTotalPaidByBankForApartment(apartmentId);
        boolean bankAllowed = requiredBank == null || requiredBank.compareTo(BigDecimal.ZERO) <= 0
                || paidByBank.compareTo(requiredBank) < 0;
        
        List<String> allowedMethods = new java.util.ArrayList<>();
        allowedMethods.add("В брой");
        if (bankAllowed) {
            allowedMethods.add("Банка");
        }
        
        BigDecimal remainingBank = paymentService.getRemainingBankAmount(apartmentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("allowedMethods", allowedMethods);
        response.put("requiredBankAmount", requiredBank);
        response.put("paidByBank", paidByBank);
        response.put("remainingBankAmount", remainingBank);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(payment -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", payment.getId());
                    data.put("amount", payment.getAmount());
                    data.put("paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : null);
                    data.put("paymentMethod", payment.getPaymentMethod());
                    data.put("paymentStage", payment.getPaymentStage());
                    data.put("invoiceNumber", payment.getInvoiceNumber());
                    data.put("isDeposit", payment.getIsDeposit());
                    data.put("notes", payment.getNotes());

                    Apartment apartment = payment.getApartment();
                    if (apartment != null) {
                        Map<String, Object> apartmentData = new HashMap<>();
                        apartmentData.put("id", apartment.getId());
                        apartmentData.put("apartmentNumber", apartment.getApartmentNumber());
                        apartmentData.put("buildingName", apartment.getBuildingName());
                        data.put("apartment", apartmentData);
                    }

                    return ResponseEntity.ok(data);
                })
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
    
    @GetMapping("/api/apartment/{apartmentId}/payment-plan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentPlanDetails(@PathVariable Long apartmentId) {
        Apartment apartment = apartmentService.getApartmentById(apartmentId).orElse(null);
        
        if (apartment == null || apartment.getPaymentPlan() == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        
        // Get payment plan
        var plan = apartment.getPaymentPlan();
        response.put("hasPaymentPlan", true);
        
        // Expected amounts per stage
        Map<String, BigDecimal> expectedAmounts = new HashMap<>();
        expectedAmounts.put("prelim", plan.getPreliminaryContractAmount() != null ? plan.getPreliminaryContractAmount() : BigDecimal.ZERO);
        expectedAmounts.put("akt14", plan.getAkt14Amount() != null ? plan.getAkt14Amount() : BigDecimal.ZERO);
        expectedAmounts.put("akt15", plan.getAkt15Amount() != null ? plan.getAkt15Amount() : BigDecimal.ZERO);
        expectedAmounts.put("akt16", plan.getAkt16Amount() != null ? plan.getAkt16Amount() : BigDecimal.ZERO);
        response.put("expectedAmounts", expectedAmounts);
        
        // Get payments by stage
        List<Payment> payments = paymentService.getPaymentsByApartmentId(apartmentId);
        
        Map<String, BigDecimal> paidByStage = new HashMap<>();
        paidByStage.put("prelim", BigDecimal.ZERO);
        paidByStage.put("akt14", BigDecimal.ZERO);
        paidByStage.put("akt15", BigDecimal.ZERO);
        paidByStage.put("akt16", BigDecimal.ZERO);
        paidByStage.put("other", BigDecimal.ZERO);
        
        for (Payment payment : payments) {
            String stage = payment.getPaymentStage();
            BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            
            if (stage != null) {
                if (stage.contains("предварителен") || stage.toLowerCase().contains("prelim")) {
                    paidByStage.put("prelim", paidByStage.get("prelim").add(amount));
                } else if (stage.contains("Акт 14") || stage.contains("акт 14")) {
                    paidByStage.put("akt14", paidByStage.get("akt14").add(amount));
                } else if (stage.contains("Акт 15") || stage.contains("акт 15")) {
                    paidByStage.put("akt15", paidByStage.get("akt15").add(amount));
                } else if (stage.contains("Акт 16") || stage.contains("акт 16")) {
                    paidByStage.put("akt16", paidByStage.get("akt16").add(amount));
                } else {
                    paidByStage.put("other", paidByStage.get("other").add(amount));
                }
            } else {
                paidByStage.put("other", paidByStage.get("other").add(amount));
            }
        }
        
        response.put("paidByStage", paidByStage);
        
        // Calculate remaining per stage
        Map<String, BigDecimal> remainingByStage = new HashMap<>();
        remainingByStage.put("prelim", expectedAmounts.get("prelim").subtract(paidByStage.get("prelim")));
        remainingByStage.put("akt14", expectedAmounts.get("akt14").subtract(paidByStage.get("akt14")));
        remainingByStage.put("akt15", expectedAmounts.get("akt15").subtract(paidByStage.get("akt15")));
        remainingByStage.put("akt16", expectedAmounts.get("akt16").subtract(paidByStage.get("akt16")));
        response.put("remainingByStage", remainingByStage);
        
        // Total amounts
        response.put("totalPrice", apartment.getTotalPrice());
        response.put("totalPaid", paymentService.getTotalPaidForApartment(apartmentId));
        response.put("totalRemaining", apartment.getTotalPrice().subtract(paymentService.getTotalPaidForApartment(apartmentId)));
        
        return ResponseEntity.ok(response);
    }
}





