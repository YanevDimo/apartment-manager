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
import java.util.Optional;
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
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
        if (buildingId == null) {
            model.addAttribute("currentBuildingId", null);
            model.addAttribute("currentBuildingName", "Не е избрана");
            model.addAttribute("currentBuildingMissing", true);
        }
        List<Client> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        return "apartments";
    }
    
    @GetMapping("/api/available")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAvailableApartmentsApi() {
        List<Apartment> apartments = apartmentService.getAllApartments().stream()
            .filter(apt -> apt.getClient() == null || !apt.getIsSold())
            .collect(Collectors.toList());
        
        List<Map<String, Object>> apartmentData = apartments.stream().map(apt -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", apt.getId());
            data.put("apartmentNumber", apt.getApartmentNumber());
            data.put("area", apt.getArea());
            data.put("totalPrice", apt.getTotalPrice());
            data.put("stage", apt.getStage());
            data.put("buildingName", apt.getBuilding() != null ? apt.getBuilding().getName() : 
                                    (apt.getBuildingName() != null ? apt.getBuildingName() : ""));
            return data;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(apartmentData);
    }
    
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApartmentsApi() {
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
        List<Apartment> apartments = buildingId != null
            ? apartmentService.getAllSoldApartmentsByBuilding(buildingId)
            : List.of();
        
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
        
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
        if (buildingId == null) {
            response.put("success", false);
            response.put("message", "Моля, изберете сграда за работа.");
            return ResponseEntity.badRequest().body(response);
        }
        var building = buildingService.getBuildingById(buildingId).orElse(null);
        if (building == null) {
            response.put("success", false);
            response.put("message", "Сградата за работа не е намерена.");
            return ResponseEntity.badRequest().body(response);
        }

        apartment.setBuilding(building);
        apartment.setBuildingName(building.getName());

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
        
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
        if (buildingId == null) {
            response.put("success", false);
            response.put("message", "Моля, изберете сграда за работа.");
            return ResponseEntity.badRequest().body(response);
        }
        var existing = apartmentService.getApartmentById(id).orElse(null);
        if (existing == null || existing.getBuilding() == null || !buildingId.equals(existing.getBuilding().getId())) {
            response.put("success", false);
            response.put("message", "Нямате достъп до този апартамент.");
            return ResponseEntity.badRequest().body(response);
        }
        var building = buildingService.getBuildingById(buildingId).orElse(null);
        if (building == null) {
            response.put("success", false);
            response.put("message", "Сградата за работа не е намерена.");
            return ResponseEntity.badRequest().body(response);
        }
        apartment.setBuilding(building);
        apartment.setBuildingName(building.getName());

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
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
            var existing = apartmentService.getApartmentById(id).orElse(null);
            if (buildingId == null || existing == null || existing.getBuilding() == null || !buildingId.equals(existing.getBuilding().getId())) {
                response.put("success", false);
                response.put("message", "Нямате достъп до този апартамент.");
                return ResponseEntity.badRequest().body(response);
            }
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
    public ResponseEntity<Map<String, Object>> getApartment(@PathVariable Long id) {
        Long buildingId = buildingService.getOrSetCurrentBuilding()
            .map(b -> b.getId())
            .orElse(null);
        
        Optional<Apartment> aptOpt = apartmentService.getApartmentById(id);
        if (aptOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Apartment apt = aptOpt.get();
        if (buildingId == null || apt.getBuilding() == null || !buildingId.equals(apt.getBuilding().getId())) {
            return ResponseEntity.notFound().build();
        }
        
        // Create a safe DTO to avoid lazy loading issues
        Map<String, Object> data = new HashMap<>();
        data.put("id", apt.getId());
        data.put("apartmentNumber", apt.getApartmentNumber());
        data.put("area", apt.getArea());
        data.put("pricePerM2", apt.getPricePerM2());
        data.put("totalPrice", apt.getTotalPrice());
        data.put("stage", apt.getStage());
        data.put("notes", apt.getNotes());
        data.put("isSold", apt.getIsSold());
        data.put("client", apt.getClient() != null ? apt.getClient().getName() : null);
        
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/api/overdue")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOverdueApartments() {
        Long buildingId = buildingService.getCurrentBuildingId().orElse(null);
        List<Apartment> apartments = buildingId != null
            ? apartmentService.getAllSoldApartmentsByBuilding(buildingId)
            : List.of();

        List<Map<String, Object>> data = apartments.stream()
            .map(this::buildOverdueDto)
            .filter(item -> {
                Object amount = item.get("overdueAmount");
                if (amount instanceof java.math.BigDecimal) {
                    return ((java.math.BigDecimal) amount).compareTo(new java.math.BigDecimal("0.01")) > 0;
                }
                return false;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/upcoming")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUpcomingPayments() {
        Long buildingId = buildingService.getCurrentBuildingId().orElse(null);
        List<Apartment> apartments = buildingId != null
            ? apartmentService.getAllSoldApartmentsByBuilding(buildingId)
            : List.of();

        List<Map<String, Object>> upcoming = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        for (Apartment apt : apartments) {
            if (apt.getPaymentPlan() == null || apt.getPayments() == null) {
                continue;
            }
            var plan = apt.getPaymentPlan();
            addUpcomingIfNeeded(upcoming, apt, "При предварителен договор", plan.getPreliminaryContractDate(), plan.getPreliminaryContractAmount(), today);
            addUpcomingIfNeeded(upcoming, apt, "Акт 14", plan.getAkt14Date(), plan.getAkt14Amount(), today);
            addUpcomingIfNeeded(upcoming, apt, "Акт 15", plan.getAkt15Date(), plan.getAkt15Amount(), today);
            addUpcomingIfNeeded(upcoming, apt, "Акт 16", plan.getAkt16Date(), plan.getAkt16Amount(), today);
        }

        upcoming.sort((a, b) -> {
            java.time.LocalDate da = (java.time.LocalDate) a.get("_date");
            java.time.LocalDate db = (java.time.LocalDate) b.get("_date");
            return da.compareTo(db);
        });

        // Remove internal date helper
        upcoming.forEach(item -> item.remove("_date"));

        return ResponseEntity.ok(upcoming);
    }

    private Map<String, Object> buildOverdueDto(Apartment apt) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", apt.getId());
        data.put("buildingName", apt.getBuildingName());
        data.put("apartmentNumber", apt.getApartmentNumber());
        data.put("stage", apt.getStage());
        data.put("clientName", apt.getClient() != null ? apt.getClient().getName() : "-");

        OverdueInfo overdueInfo = getOverdueInfo(apt);
        data.put("overdueAmount", overdueInfo.amount);
        data.put("daysOverdue", overdueInfo.days);
        return data;
    }

    private void addUpcomingIfNeeded(List<Map<String, Object>> upcoming, Apartment apt, String stageLabel,
                                     java.time.LocalDate date, java.math.BigDecimal expectedAmount,
                                     java.time.LocalDate today) {
        if (date == null || expectedAmount == null || expectedAmount.compareTo(new java.math.BigDecimal("0.01")) <= 0) {
            return;
        }
        if (!date.isAfter(today)) {
            return;
        }

        java.math.BigDecimal paid = getPaidForStage(apt, stageLabel);
        if (paid.compareTo(expectedAmount) >= 0) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", apt.getId());
        data.put("buildingName", apt.getBuildingName());
        data.put("apartmentNumber", apt.getApartmentNumber());
        data.put("clientName", apt.getClient() != null ? apt.getClient().getName() : "-");
        data.put("stage", stageLabel);
        data.put("expectedAmount", expectedAmount.subtract(paid));
        data.put("date", date.toString());
        data.put("_date", date);
        upcoming.add(data);
    }

    private OverdueInfo getOverdueInfo(Apartment apt) {
        java.time.LocalDate today = java.time.LocalDate.now();
        var plan = apt.getPaymentPlan();
        if (plan == null) {
            return new OverdueInfo(java.math.BigDecimal.ZERO, 0);
        }

        OverdueInfo result = new OverdueInfo(java.math.BigDecimal.ZERO, 0);
        result = pickOverdueForStage(result, apt, "При предварителен договор", plan.getPreliminaryContractDate(), plan.getPreliminaryContractAmount(), today);
        result = pickOverdueForStage(result, apt, "Акт 14", plan.getAkt14Date(), plan.getAkt14Amount(), today);
        result = pickOverdueForStage(result, apt, "Акт 15", plan.getAkt15Date(), plan.getAkt15Amount(), today);
        result = pickOverdueForStage(result, apt, "Акт 16", plan.getAkt16Date(), plan.getAkt16Amount(), today);
        return result;
    }

    private OverdueInfo pickOverdueForStage(OverdueInfo current, Apartment apt, String stageLabel,
                                            java.time.LocalDate date, java.math.BigDecimal expectedAmount,
                                            java.time.LocalDate today) {
        if (date == null || expectedAmount == null || expectedAmount.compareTo(new java.math.BigDecimal("0.01")) <= 0) {
            return current;
        }
        if (!date.isBefore(today)) {
            return current;
        }
        java.math.BigDecimal paid = getPaidForStage(apt, stageLabel);
        java.math.BigDecimal remaining = expectedAmount.subtract(paid);
        if (remaining.compareTo(new java.math.BigDecimal("0.01")) <= 0) {
            return current;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(date, today);
        if (days > current.days) {
            return new OverdueInfo(remaining, (int) days);
        }
        return current;
    }

    private java.math.BigDecimal getPaidForStage(Apartment apt, String stageLabel) {
        if (apt.getPayments() == null) {
            return java.math.BigDecimal.ZERO;
        }
        return apt.getPayments().stream()
            .filter(p -> p.getPaymentStage() != null)
            .filter(p -> {
                String s = p.getPaymentStage();
                if (stageLabel.contains("предварителен")) {
                    return s.toLowerCase().contains("предварителен") || s.toLowerCase().contains("prelim");
                }
                if (stageLabel.equals("Акт 14")) {
                    return s.contains("Акт 14") || s.contains("акт 14");
                }
                if (stageLabel.equals("Акт 15")) {
                    return s.contains("Акт 15") || s.contains("акт 15");
                }
                if (stageLabel.equals("Акт 16")) {
                    return s.contains("Акт 16") || s.contains("акт 16");
                }
                return false;
            })
            .map(p -> p.getAmount() != null ? p.getAmount() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private static class OverdueInfo {
        final java.math.BigDecimal amount;
        final int days;

        OverdueInfo(java.math.BigDecimal amount, int days) {
            this.amount = amount;
            this.days = days;
        }
    }
    
    @PostMapping("/api/stage/global")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateGlobalStage(@RequestParam String stage) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long buildingId = buildingService.getOrSetCurrentBuilding()
                .map(b -> b.getId())
                .orElse(null);
            if (buildingId == null) {
                response.put("success", false);
                response.put("message", "Моля, изберете сграда за работа.");
                return ResponseEntity.badRequest().body(response);
            }
            apartmentService.updateAllApartmentsStageByBuilding(buildingId, stage);
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


