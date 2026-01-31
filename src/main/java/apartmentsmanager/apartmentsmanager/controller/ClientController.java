package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clients")
public class ClientController {
    
    private final ClientService clientService;
    private final ApartmentService apartmentService;
    
    public ClientController(ClientService clientService, ApartmentService apartmentService) {
        this.clientService = clientService;
        this.apartmentService = apartmentService;
    }
    
    @GetMapping
    public String clientsPage(@RequestParam(required = false) String search, Model model) {
        List<Client> clients;
        if (search != null && !search.trim().isEmpty()) {
            clients = clientService.searchClients(search.trim());
            model.addAttribute("searchTerm", search);
        } else {
            clients = clientService.getAllClients();
        }
        model.addAttribute("clients", clients);
        return "clients";
    }
    
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllClientsApi() {
        List<Client> clients = clientService.getAllClients();
        
        List<Map<String, Object>> clientData = clients.stream().map(client -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", client.getId());
            data.put("name", client.getName());
            data.put("phone", client.getPhone());
            data.put("email", client.getEmail());
            data.put("address", client.getAddress());
            data.put("notes", client.getNotes());
            data.put("createdAt", client.getCreatedAt() != null ? client.getCreatedAt().toString() : null);
            data.put("updatedAt", client.getUpdatedAt() != null ? client.getUpdatedAt().toString() : null);
            // Get apartments count using repository query to avoid lazy loading
            Long apartmentsCount = clientService.getApartmentCountForClient(client.getId());
            data.put("apartmentsCount", apartmentsCount != null ? apartmentsCount : 0);
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("clients", clientData);
        response.put("total", clients.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchClientsApi(@RequestParam(required = false) String q) {
        List<Client> clients = clientService.searchClients(q);
        
        List<Map<String, Object>> clientData = clients.stream().map(client -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", client.getId());
            data.put("name", client.getName());
            data.put("phone", client.getPhone());
            data.put("email", client.getEmail());
            data.put("address", client.getAddress());
            data.put("notes", client.getNotes());
            data.put("createdAt", client.getCreatedAt() != null ? client.getCreatedAt().toString() : null);
            data.put("updatedAt", client.getUpdatedAt() != null ? client.getUpdatedAt().toString() : null);
            // Get apartments count using repository query to avoid lazy loading
            Long apartmentsCount = clientService.getApartmentCountForClient(client.getId());
            data.put("apartmentsCount", apartmentsCount != null ? apartmentsCount : 0);
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("clients", clientData);
        response.put("total", clients.size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addClient(@Valid @RequestBody Client client,
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
        
        try {
            Client saved = clientService.saveClient(client);
            response.put("success", true);
            response.put("message", "Клиентът е добавен успешно");
            response.put("client", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при добавяне на клиент: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateClient(@PathVariable Long id,
                                                           @Valid @RequestBody Client client,
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
        
        Client existingClient = clientService.getClientById(id).orElse(null);
        if (existingClient == null) {
            response.put("success", false);
            response.put("message", "Клиентът не е намерен");
            return ResponseEntity.notFound().build();
        }
        
        client.setId(id);
        
        try {
            Client saved = clientService.saveClient(client);
            response.put("success", true);
            response.put("message", "Клиентът е обновен успешно");
            response.put("client", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при обновяване на клиент: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Client client = clientService.getClientById(id).orElse(null);
        if (client == null) {
            response.put("success", false);
            response.put("message", "Клиентът не е намерен");
            return ResponseEntity.notFound().build();
        }
        
        // Check if client has apartments
        if (client.getApartments() != null && !client.getApartments().isEmpty()) {
            response.put("success", false);
            response.put("message", "Не можете да изтриете клиент с асоциирани апартаменти");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            clientService.deleteClient(id);
            response.put("success", true);
            response.put("message", "Клиентът е изтрит успешно");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при изтриване: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    public String clientDetailPage(@PathVariable Long id, Model model) {
        Client client = clientService.getClientByIdWithApartmentsAndPayments(id).orElse(null);
        if (client == null) {
            return "redirect:/clients";
        }
        
        // Calculate statistics
        int apartmentsCount = client.getApartments() != null ? client.getApartments().size() : 0;
        
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalBankPayments = BigDecimal.ZERO;
        BigDecimal totalCashPayments = BigDecimal.ZERO;
        
        if (client.getApartments() != null && !client.getApartments().isEmpty()) {
            for (var apt : client.getApartments()) {
                if (apt.getTotalPrice() != null) {
                    totalValue = totalValue.add(apt.getTotalPrice());
                }
                BigDecimal aptPaid = apt.getTotalPaid();
                if (aptPaid != null) {
                    totalPaid = totalPaid.add(aptPaid);
                }
                
                // Calculate payment methods breakdown
                if (apt.getPayments() != null) {
                    for (var payment : apt.getPayments()) {
                        if (payment.getAmount() != null) {
                            if ("Банка".equals(payment.getPaymentMethod()) || 
                                "Bank Transfer".equals(payment.getPaymentMethod()) ||
                                "Bank".equals(payment.getPaymentMethod())) {
                                totalBankPayments = totalBankPayments.add(payment.getAmount());
                            } else if ("В брой".equals(payment.getPaymentMethod()) || 
                                      "Cash".equals(payment.getPaymentMethod())) {
                                totalCashPayments = totalCashPayments.add(payment.getAmount());
                            }
                        }
                    }
                }
            }
        }
        
        BigDecimal remaining = totalValue.subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }
        
        model.addAttribute("client", client);
        model.addAttribute("apartmentsCount", apartmentsCount);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("remaining", remaining);
        model.addAttribute("totalBankPayments", totalBankPayments);
        model.addAttribute("totalCashPayments", totalCashPayments);
        
        return "client_detail";
    }
    
    @PostMapping("/api/{id}/add-purchases")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addPurchasesToClient(@PathVariable Long id,
                                                                     @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        Client client = clientService.getClientById(id).orElse(null);
        if (client == null) {
            response.put("success", false);
            response.put("message", "Клиентът не е намерен");
            return ResponseEntity.notFound().build();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> purchases = (List<Map<String, Object>>) requestBody.get("purchases");
        
        if (purchases == null || purchases.isEmpty()) {
            response.put("success", false);
            response.put("message", "Не са избрани обекти");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            int updatedCount = 0;
            for (Map<String, Object> purchase : purchases) {
                Integer aptId = (Integer) purchase.get("apartmentId");
                var apartment = apartmentService.getApartmentById(aptId.longValue()).orElse(null);
                
                if (apartment != null && apartment.getClient() == null) {
                    apartment.setClient(client);
                    apartment.setIsSold(true);
                    
                    // Set final price after discount
                    if (purchase.containsKey("finalPrice")) {
                        // Use setScale to ensure exactly 2 decimal places
                        BigDecimal finalPrice = new BigDecimal(purchase.get("finalPrice").toString())
                                                   .setScale(2, java.math.RoundingMode.HALF_UP);
                        apartment.setTotalPrice(finalPrice);
                        
                        // Calculate price per m2 if area is available
                        if (apartment.getArea() != null && apartment.getArea().compareTo(BigDecimal.ZERO) > 0) {
                            apartment.setPricePerM2(finalPrice.divide(apartment.getArea(), 2, java.math.RoundingMode.HALF_UP));
                        }
                    }
                    
                    // Set payment plan with calculated amounts
                    if (purchase.containsKey("paymentPlan")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> planData = (Map<String, Object>) purchase.get("paymentPlan");
                        apartmentsmanager.apartmentsmanager.entity.PaymentPlan plan = new apartmentsmanager.apartmentsmanager.entity.PaymentPlan();
                        
                        // Convert amounts with proper precision (2 decimal places)
                        BigDecimal prelimAmount = planData.get("preliminaryContractAmount") != null ? 
                            new BigDecimal(planData.get("preliminaryContractAmount").toString()).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal akt14Amount = planData.get("akt14Amount") != null ? 
                            new BigDecimal(planData.get("akt14Amount").toString()).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal akt15Amount = planData.get("akt15Amount") != null ? 
                            new BigDecimal(planData.get("akt15Amount").toString()).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal akt16Amount = planData.get("akt16Amount") != null ? 
                            new BigDecimal(planData.get("akt16Amount").toString()).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        
                        // Calculate total of payment plan amounts
                        BigDecimal planTotal = prelimAmount.add(akt14Amount).add(akt15Amount).add(akt16Amount);
                        BigDecimal finalPrice = apartment.getTotalPrice();
                        
                        // If there's a rounding difference, add it to the last stage (Akt 16)
                        if (finalPrice != null) {
                            BigDecimal difference = finalPrice.subtract(planTotal);
                            if (difference.abs().compareTo(new BigDecimal("0.10")) < 0) { // If difference is less than 10 cents
                                akt16Amount = akt16Amount.add(difference).setScale(2, java.math.RoundingMode.HALF_UP);
                            }
                        }
                        
                        plan.setPreliminaryContractAmount(prelimAmount);
                        plan.setAkt14Amount(akt14Amount);
                        plan.setAkt15Amount(akt15Amount);
                        plan.setAkt16Amount(akt16Amount);
                        
                        apartment.setPaymentPlan(plan);
                    }
                    
                    // Store package info in notes
                    String packageType = (String) purchase.get("packageType");
                    String packageInfo = "Пакет за плащане: " + getPackageName(packageType);
                    
                    if (apartment.getNotes() != null && !apartment.getNotes().isEmpty()) {
                        apartment.setNotes(apartment.getNotes() + "\n" + packageInfo);
                    } else {
                        apartment.setNotes(packageInfo);
                    }
                    
                    apartmentService.saveApartment(apartment);
                    updatedCount++;
                }
            }
            
            response.put("success", true);
            response.put("message", updatedCount + " обекта добавени успешно");
            response.put("count", updatedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при добавяне: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private String getPackageName(String packageType) {
        return switch (packageType) {
            case "standard" -> "Стандартен";
            case "balanced" -> "Балансиран";
            case "optimal" -> "Оптимален";
            case "investment" -> "Инвестиционен";
            case "profitable" -> "Печеливш";
            case "custom" -> "Индивидуален";
            default -> packageType;
        };
    }
    
    @GetMapping("/edit/{id}")
    public String editClientPage(@PathVariable Long id, Model model) {
        Client client = clientService.getClientById(id).orElse(null);
        if (client == null) {
            return "redirect:/clients";
        }
        model.addAttribute("client", client);
        return "edit_client";
    }
    
    @PostMapping("/edit/{id}")
    public String updateClient(@PathVariable Long id,
                              @Valid @ModelAttribute Client client,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("client", client);
            return "edit_client";
        }
        
        Client existingClient = clientService.getClientById(id).orElse(null);
        if (existingClient == null) {
            redirectAttributes.addFlashAttribute("error", "Клиентът не е намерен");
            return "redirect:/clients";
        }
        
        client.setId(id);
        
        try {
            clientService.saveClient(client);
            redirectAttributes.addFlashAttribute("success", "Клиентът е обновен успешно!");
            return "redirect:/clients/" + id;
        } catch (Exception e) {
            bindingResult.reject("error.client", "Грешка при обновяване на клиент: " + e.getMessage());
            model.addAttribute("client", client);
            return "edit_client";
        }
    }
}

