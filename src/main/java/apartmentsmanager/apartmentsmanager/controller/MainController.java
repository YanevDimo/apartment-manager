package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class MainController {
    
    private final ClientService clientService;
    private final ApartmentService apartmentService;
    private final BuildingService buildingService;
    
    public MainController(ClientService clientService, ApartmentService apartmentService, BuildingService buildingService) {
        this.clientService = clientService;
        this.apartmentService = apartmentService;
        this.buildingService = buildingService;
    }
    
    @GetMapping("/")
    public String index() {
        // Redirect to dashboard for admin, public home for others
        return "redirect:/index";
    }
    
    @GetMapping("/index")
    public String indexPage(Model model) {
        buildingService.getOrSetCurrentBuilding().ifPresent(building -> {
            model.addAttribute("currentBuildingId", building.getId());
            model.addAttribute("currentBuildingName", building.getName());
        });
        return "dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/index";
    }
    
    @GetMapping("/add-apartment")
    public String addApartmentPage(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        var currentBuilding = buildingService.getOrSetCurrentBuilding().orElse(null);
        if (currentBuilding == null) {
            model.addAttribute("error", "Моля, изберете сграда за работа преди добавяне на апартаменти.");
            return "redirect:/buildings";
        }
        model.addAttribute("currentBuildingId", currentBuilding.getId());
        model.addAttribute("currentBuildingName", currentBuilding.getName());
        model.addAttribute("currentBuildingStage", currentBuilding.getStage());
        return "add_apartment";
    }
    
    @PostMapping("/add-apartment")
    public String addApartment(HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get buyer
            String buyer = request.getParameter("buyer");
            Client client = null;
            if (buyer != null && !buyer.trim().isEmpty()) {
                Optional<Client> existingClient = clientService.getAllClients().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(buyer.trim()))
                    .findFirst();
                
                if (existingClient.isPresent()) {
                    client = existingClient.get();
                } else {
                    Client newClient = new Client();
                    newClient.setName(buyer.trim());
                    client = clientService.saveClient(newClient);
                }
            }
            
            // Use current building (global selection)
            Building building = null;
            var currentBuilding = buildingService.getOrSetCurrentBuilding().orElse(null);
            if (currentBuilding != null) {
                building = currentBuilding;
            }
            
            if (building == null) {
                redirectAttributes.addFlashAttribute("error", "Моля, изберете сграда за работа преди добавяне на апартаменти!");
                return "redirect:/add-apartment";
            }
            
            // Process nested objects[0][apartment], objects[0][area], etc.
            int objectIndex = 0;
            boolean hasMoreObjects = true;
            
            while (hasMoreObjects) {
                String apartmentNumber = request.getParameter("objects[" + objectIndex + "][apartment]");
                String area = request.getParameter("objects[" + objectIndex + "][area]");
                String pricePerM2 = request.getParameter("objects[" + objectIndex + "][price_per_m2]");
                
                if (apartmentNumber != null && !apartmentNumber.trim().isEmpty() &&
                    area != null && !area.trim().isEmpty() &&
                    pricePerM2 != null && !pricePerM2.trim().isEmpty()) {
                    
                    try {
                        Apartment apartment = new Apartment();
                        apartment.setBuilding(building);
                        apartment.setBuildingName(building.getName()); // For backward compatibility
                        apartment.setApartmentNumber(apartmentNumber.trim());
                        apartment.setArea(new BigDecimal(area));
                        apartment.setPricePerM2(new BigDecimal(pricePerM2));
                        apartment.setClient(client);
                        apartment.setIsSold(true);
                        
                        apartmentService.saveApartment(apartment);
                    } catch (NumberFormatException e) {
                        redirectAttributes.addFlashAttribute("error", 
                            "Невалидни числени стойности за обект #" + (objectIndex + 1));
                        return "redirect:/add-apartment";
                    }
                    
                    objectIndex++;
                } else {
                    hasMoreObjects = false;
                }
            }
            
            if (objectIndex == 0) {
                redirectAttributes.addFlashAttribute("error", "Моля, въведете поне един обект!");
                return "redirect:/add-apartment";
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Успешно добавени " + objectIndex + " апартамент(а)!");
            return "redirect:/apartments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Грешка при добавяне на апартамент: " + e.getMessage());
            return "redirect:/add-apartment";
        }
    }
    
    @GetMapping("/add-building")
    public String addBuildingPage() {
        // Redirect to the proper BuildingController endpoint
        return "redirect:/buildings/add";
    }
    
    @GetMapping("/add-client")
    public String addClientPage(Model model) {
        model.addAttribute("client", new Client());
        return "add_client";
    }
    
    @PostMapping("/add-client")
    public String addClient(@Valid @ModelAttribute Client client,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "add_client";
        }
        
        try {
            clientService.saveClient(client);
            redirectAttributes.addFlashAttribute("success", "Клиентът е добавен успешно!");
            return "redirect:/clients";
        } catch (Exception e) {
            bindingResult.reject("error.client", "Грешка при добавяне на клиент: " + e.getMessage());
            return "add_client";
        }
    }
}
