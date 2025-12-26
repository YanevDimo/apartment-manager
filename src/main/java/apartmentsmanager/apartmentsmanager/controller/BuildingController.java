package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/buildings")
public class BuildingController {
    
    private final BuildingService buildingService;
    
    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }
    
    @GetMapping
    public String buildingsPage(Model model) {
        try {
            java.util.List<Building> buildings = buildingService.getAllBuildings();
            model.addAttribute("buildings", buildings);
            model.addAttribute("buildingsCount", buildings.size());
            
            // Select current building (first active building, or first building if no active)
            Building currentBuilding = null;
            if (buildings != null && !buildings.isEmpty()) {
                // Try to find an active building first
                currentBuilding = buildings.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus().equals("активна"))
                    .findFirst()
                    .orElse(buildings.get(0)); // If no active, use first building
                
                // Load statistics for current building
                int apartmentsCount = currentBuilding.getApartments() != null ? currentBuilding.getApartments().size() : 0;
                int garagesCount = currentBuilding.getGarages() != null ? currentBuilding.getGarages().size() : 0;
                int basementsCount = currentBuilding.getBasements() != null ? currentBuilding.getBasements().size() : 0;
                int parkingSpacesCount = currentBuilding.getParkingSpaces() != null ? currentBuilding.getParkingSpaces().size() : 0;
                int commercialSpacesCount = currentBuilding.getCommercialSpaces() != null ? currentBuilding.getCommercialSpaces().size() : 0;
                
                model.addAttribute("currentBuilding", currentBuilding);
                model.addAttribute("apartmentsCount", apartmentsCount);
                model.addAttribute("garagesCount", garagesCount);
                model.addAttribute("basementsCount", basementsCount);
                model.addAttribute("parkingSpacesCount", parkingSpacesCount);
                model.addAttribute("commercialSpacesCount", commercialSpacesCount);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Грешка при зареждане на сградите: " + e.getMessage());
            model.addAttribute("buildings", java.util.Collections.emptyList());
            model.addAttribute("buildingsCount", 0);
        }
        return "buildings";
    }
    
    @GetMapping("/add")
    public String addBuildingPage(Model model) {
        if (!model.containsAttribute("building")) {
            Building building = new Building();
            // Lists are initialized by getters, but set them explicitly for safety
            building.setApartments(new java.util.ArrayList<>());
            building.setGarages(new java.util.ArrayList<>());
            building.setBasements(new java.util.ArrayList<>());
            building.setParkingSpaces(new java.util.ArrayList<>());
            building.setCommercialSpaces(new java.util.ArrayList<>());
            model.addAttribute("building", building);
        }
        return "add_building";
    }
    
    // Test endpoint to verify database connection
    @GetMapping("/test")
    @org.springframework.web.bind.annotation.ResponseBody
    public String testBuildingService() {
        try {
            long count = buildingService.getAllBuildings().size();
            return "Building service works! Current building count: " + count;
        } catch (Exception e) {
            return "ERROR: " + e.getMessage() + "\n" + 
                   java.util.Arrays.toString(e.getStackTrace());
        }
    }
    
    // API endpoint to get building count
    @GetMapping("/count")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> getBuildingCount() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            long count = buildingService.getAllBuildings().size();
            result.put("success", true);
            result.put("count", count);
            result.put("message", "Общ брой сгради: " + count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("count", 0);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/add")
    public String addBuilding(@Valid @ModelAttribute("building") Building building,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        // Always initialize lists first to avoid any lazy loading issues
        if (building == null) {
            building = new Building();
        }
        building.setApartments(new java.util.ArrayList<>());
        building.setGarages(new java.util.ArrayList<>());
        building.setBasements(new java.util.ArrayList<>());
        building.setParkingSpaces(new java.util.ArrayList<>());
        building.setCommercialSpaces(new java.util.ArrayList<>());
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("building", building);
            return "add_building";
        }
        
        // Validate name is not empty
        if (building.getName() == null || building.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "error.name", "Името на сградата е задължително");
            model.addAttribute("building", building);
            return "add_building";
        }
        
        // Check for duplicate name
        if (buildingService.buildingExists(building.getName().trim())) {
            bindingResult.rejectValue("name", "error.name", "Сграда с това име вече съществува");
            model.addAttribute("building", building);
            return "add_building";
        }
        
        try {
            // Trim name
            building.setName(building.getName().trim());
            
            // Save building
            Building savedBuilding = buildingService.saveBuilding(building);
            
            redirectAttributes.addFlashAttribute("success", 
                "Сградата '" + savedBuilding.getName() + "' е добавена успешно! Сега можете да добавите апартаменти, гаражи и мазета.");
            return "redirect:/buildings";
        } catch (Exception e) {
            // Log full stack trace for debugging
            System.err.println("ERROR saving building: " + e.getMessage());
            e.printStackTrace();
            
            bindingResult.reject("error.building", "Грешка при добавяне на сграда: " + e.getMessage());
            model.addAttribute("building", building);
            model.addAttribute("error", "Грешка при добавяне на сграда: " + e.getMessage() + ". Проверете логовете за повече детайли.");
            return "add_building";
        }
    }
    
    // View building details with all information
    @GetMapping("/{id}")
    public String viewBuilding(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Building building = buildingService.getBuildingById(id)
                .orElseThrow(() -> new RuntimeException("Сграда с ID " + id + " не е намерена"));
            
            // Load related entities (they are lazy loaded, so we need to access them)
            int apartmentsCount = building.getApartments() != null ? building.getApartments().size() : 0;
            int garagesCount = building.getGarages() != null ? building.getGarages().size() : 0;
            int basementsCount = building.getBasements() != null ? building.getBasements().size() : 0;
            int parkingSpacesCount = building.getParkingSpaces() != null ? building.getParkingSpaces().size() : 0;
            int commercialSpacesCount = building.getCommercialSpaces() != null ? building.getCommercialSpaces().size() : 0;
            
            model.addAttribute("building", building);
            model.addAttribute("apartmentsCount", apartmentsCount);
            model.addAttribute("garagesCount", garagesCount);
            model.addAttribute("basementsCount", basementsCount);
            model.addAttribute("parkingSpacesCount", parkingSpacesCount);
            model.addAttribute("commercialSpacesCount", commercialSpacesCount);
            
            return "view_building";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Сградата не е намерена: " + e.getMessage());
            return "redirect:/buildings";
        }
    }
    
    // Edit building - GET
    @GetMapping("/edit/{id}")
    public String editBuildingPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Building building = buildingService.getBuildingById(id)
                .orElseThrow(() -> new RuntimeException("Сграда с ID " + id + " не е намерена"));
            
            // Initialize lists if null
            if (building.getApartments() == null) {
                building.setApartments(new java.util.ArrayList<>());
            }
            if (building.getGarages() == null) {
                building.setGarages(new java.util.ArrayList<>());
            }
            if (building.getBasements() == null) {
                building.setBasements(new java.util.ArrayList<>());
            }
            
            model.addAttribute("building", building);
            return "edit_building";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Сградата не е намерена: " + e.getMessage());
            return "redirect:/buildings";
        }
    }
    
    // Update building - POST
    @PostMapping("/edit/{id}")
    public String updateBuilding(@PathVariable Long id,
                                @Valid @ModelAttribute("building") Building building,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            // Check if building exists
            Building existingBuilding = buildingService.getBuildingById(id)
                .orElseThrow(() -> new RuntimeException("Сграда с ID " + id + " не е намерена"));
            
            // Initialize lists
            building.setApartments(existingBuilding.getApartments() != null ? existingBuilding.getApartments() : new java.util.ArrayList<>());
            building.setGarages(existingBuilding.getGarages() != null ? existingBuilding.getGarages() : new java.util.ArrayList<>());
            building.setBasements(existingBuilding.getBasements() != null ? existingBuilding.getBasements() : new java.util.ArrayList<>());
            
            if (bindingResult.hasErrors()) {
                model.addAttribute("building", building);
                return "edit_building";
            }
            
            // Validate name is not empty
            if (building.getName() == null || building.getName().trim().isEmpty()) {
                bindingResult.rejectValue("name", "error.name", "Името на сградата е задължително");
                model.addAttribute("building", building);
                return "edit_building";
            }
            
            // Check for duplicate name (excluding current building)
            Optional<Building> duplicateBuilding = buildingService.getBuildingByName(building.getName().trim());
            if (duplicateBuilding.isPresent() && !duplicateBuilding.get().getId().equals(id)) {
                bindingResult.rejectValue("name", "error.name", "Сграда с това име вече съществува");
                model.addAttribute("building", building);
                return "edit_building";
            }
            
            // Set ID to ensure update instead of insert
            building.setId(id);
            building.setCreatedAt(existingBuilding.getCreatedAt()); // Preserve creation date
            
            // Trim name
            building.setName(building.getName().trim());
            
            // Save building
            Building savedBuilding = buildingService.saveBuilding(building);
            
            redirectAttributes.addFlashAttribute("success", 
                "Сградата '" + savedBuilding.getName() + "' е обновена успешно!");
            return "redirect:/buildings";
        } catch (Exception e) {
            System.err.println("ERROR updating building: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("error", "Грешка при обновяване на сграда: " + e.getMessage());
            return "redirect:/buildings/edit/" + id;
        }
    }
    
    // Delete building - POST
    @PostMapping("/delete")
    public String deleteBuilding(@RequestParam("buildingId") Long id, RedirectAttributes redirectAttributes) {
        try {
            Building building = buildingService.getBuildingById(id)
                .orElseThrow(() -> new RuntimeException("Сграда с ID " + id + " не е намерена"));
            
            String buildingName = building.getName();
            buildingService.deleteBuilding(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Сградата '" + buildingName + "' е изтрита успешно!");
        } catch (Exception e) {
            System.err.println("ERROR deleting building: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Грешка при изтриване на сграда: " + e.getMessage());
        }
        return "redirect:/buildings";
    }
}



