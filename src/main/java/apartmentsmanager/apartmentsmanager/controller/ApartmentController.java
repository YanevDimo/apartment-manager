package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/apartments")
public class ApartmentController {
    
    private final ApartmentService apartmentService;
    private final ClientService clientService;
    private final BuildingService buildingService;
    
    public ApartmentController(ApartmentService apartmentService, ClientService clientService, BuildingService buildingService) {
        this.apartmentService = apartmentService;
        this.clientService = clientService;
        this.buildingService = buildingService;
    }
    
    @GetMapping
    public String apartmentsPage(Model model) {
        List<Apartment> apartments = apartmentService.getAllSoldApartments();
        List<Client> clients = clientService.getAllClients();
        
        model.addAttribute("apartments", apartments);
        model.addAttribute("clients", clients);
        return "apartments";
    }
    
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApartmentsApi() {
        List<Apartment> apartments = apartmentService.getAllSoldApartments();
        
        List<Map<String, Object>> apartmentData = apartments.stream().map(apt -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", apt.getId());
            data.put("buildingName", apt.getBuildingName());
            data.put("apartmentNumber", apt.getApartmentNumber());
            data.put("area", apt.getArea());
            data.put("pricePerM2", apt.getPricePerM2());
            data.put("totalPrice", apt.getTotalPrice());
            data.put("stage", apt.getStage());
            data.put("client", apt.getClient() != null ? apt.getClient().getName() : "");
            data.put("totalPaid", apt.getTotalPaid());
            data.put("remainingPayment", apt.getRemainingPayment());
            data.put("hasOverduePayments", apt.hasOverduePayments());
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("apartments", apartmentData);
        response.put("total", apartments.size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addApartment(@Valid @RequestBody Apartment apartment, 
                                                           BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Грешка при валидация на данните");
            response.put("errors", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check for duplicate
        if (apartmentService.apartmentExists(apartment.getBuildingName(), 
                                            apartment.getApartmentNumber(), null)) {
            response.put("success", false);
            response.put("message", "Апартамент с този номер вече съществува в тази сграда");
            return ResponseEntity.badRequest().body(response);
        }
        
        apartment.setIsSold(true);
        Apartment saved = apartmentService.saveApartment(apartment);
        
        response.put("success", true);
        response.put("message", "Апартаментът е добавен успешно");
        response.put("apartment", saved);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateApartment(@PathVariable Long id,
                                                               @Valid @RequestBody Apartment apartment,
                                                               BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Грешка при валидация на данните");
            response.put("errors", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check for duplicate (excluding current apartment)
        if (apartmentService.apartmentExists(apartment.getBuildingName(), 
                                            apartment.getApartmentNumber(), id)) {
            response.put("success", false);
            response.put("message", "Апартамент с този номер вече съществува в тази сграда");
            return ResponseEntity.badRequest().body(response);
        }
        
        apartment.setId(id);
        Apartment saved = apartmentService.saveApartment(apartment);
        
        response.put("success", true);
        response.put("message", "Апартаментът е обновен успешно");
        response.put("apartment", saved);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteApartment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            apartmentService.deleteApartment(id);
            response.put("success", true);
            response.put("message", "Апартаментът е изтрит успешно");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при изтриване: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Apartment> getApartment(@PathVariable Long id) {
        return apartmentService.getApartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/overdue")
    @ResponseBody
    public ResponseEntity<List<Apartment>> getOverdueApartments() {
        Long buildingId = buildingService.getCurrentBuildingId().orElse(null);
        List<Apartment> overdue = buildingId != null
            ? apartmentService.getApartmentsWithOverduePaymentsByBuilding(buildingId)
            : List.of();
        return ResponseEntity.ok(overdue);
    }
    
    @PostMapping("/api/stage/global")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateGlobalStage(@RequestParam String stage) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            apartmentService.updateAllApartmentsStage(stage);
            response.put("success", true);
            response.put("message", "Етапът на всички апартаменти е обновен");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/clients")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getClientsApi() {
        List<Client> clients = clientService.getAllClients();
        
        List<Map<String, Object>> clientData = clients.stream().map(client -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", client.getId());
            data.put("name", client.getName());
            data.put("phone", client.getPhone());
            data.put("email", client.getEmail());
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("clients", clientData);
        response.put("total", clients.size());
        
        return ResponseEntity.ok(response);
    }
}


