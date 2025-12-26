package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import apartmentsmanager.apartmentsmanager.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/system")
public class SystemResetController {
    
    private final ApartmentService apartmentService;
    private final ClientService clientService;
    private final PaymentService paymentService;
    
    @Autowired
    public SystemResetController(ApartmentService apartmentService,
                                ClientService clientService,
                                PaymentService paymentService) {
        this.apartmentService = apartmentService;
        this.clientService = clientService;
        this.paymentService = paymentService;
    }
    
    @GetMapping("/reset")
    public String resetPage(Model model) {
        // Get statistics for confirmation
        long apartmentCount = apartmentService.getTotalApartmentsCount();
        long clientCount = clientService.getAllClients().size();
        
        model.addAttribute("apartmentCount", apartmentCount);
        model.addAttribute("clientCount", clientCount);
        
        return "system_reset";
    }
    
    @GetMapping("/reset/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResetConfirmation() {
        Map<String, Object> response = new HashMap<>();
        
        long apartmentCount = apartmentService.getTotalApartmentsCount();
        long clientCount = clientService.getAllClients().size();
        
        response.put("apartmentCount", apartmentCount);
        response.put("clientCount", clientCount);
        response.put("warning", "Това действие ще изтрие ВСИЧКИ данни от системата!");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset/execute")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeReset(@RequestParam(required = false) String confirmation) {
        Map<String, Object> response = new HashMap<>();
        
        // Multi-step confirmation
        if (!"CONFIRM_DELETE_ALL_DATA".equals(confirmation)) {
            response.put("success", false);
            response.put("message", "Невалидно потвърждение. Моля, потвърдете изтриването.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Delete all payments first (due to foreign key constraints)
            // Note: This is a simplified approach. In production, you might want to use
            // cascade delete or delete in proper order
            
            // Get all apartments and delete their payments
            apartmentService.getAllApartments().forEach(apartment -> {
                paymentService.getPaymentsByApartmentId(apartment.getId())
                    .forEach(payment -> paymentService.deletePayment(payment.getId()));
            });
            
            // Delete all apartments
            apartmentService.getAllApartments().forEach(apartment -> {
                apartmentService.deleteApartment(apartment.getId());
            });
            
            // Delete all clients
            clientService.getAllClients().forEach(client -> {
                clientService.deleteClient(client.getId());
            });
            
            response.put("success", true);
            response.put("message", "Всички данни са изтрити успешно");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при изтриване на данните: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}



