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
    public String indexPage() {
        return "dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/index";
    }
    
    @GetMapping("/add-apartment")
    public String addApartmentPage(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("buildings", buildingService.getAllBuildings());
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
            
            // Get building (by name or ID)
            String buildingName = request.getParameter("buildingName");
            String buildingIdStr = request.getParameter("buildingId");
            Building building = null;
            
            if (buildingIdStr != null && !buildingIdStr.trim().isEmpty()) {
                try {
                    Long buildingId = Long.parseLong(buildingIdStr);
                    building = buildingService.getBuildingById(buildingId)
                        .orElse(null);
                } catch (NumberFormatException e) {
                    // Invalid building ID, try by name
                }
            }
            
            if (building == null && buildingName != null && !buildingName.trim().isEmpty()) {
                building = buildingService.getBuildingByName(buildingName.trim())
                    .orElse(null);
                
                // If building doesn't exist, create it
                if (building == null) {
                    Building newBuilding = new Building();
                    newBuilding.setName(buildingName.trim());
                    newBuilding.setStatus("активна");
                    building = buildingService.saveBuilding(newBuilding);
                }
            }
            
            if (building == null) {
                redirectAttributes.addFlashAttribute("error", "Моля, изберете или създайте сграда!");
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
        
        // Check for duplicate EGN if provided
        if (client.getEgn() != null && !client.getEgn().trim().isEmpty()) {
            if (clientService.findByEgn(client.getEgn()).isPresent()) {
                bindingResult.rejectValue("egn", "error.egn", "Клиент с този ЕГН/ЕИК вече съществува");
                return "add_client";
            }
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
