package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    
    /** Списък със сгради. Маппинг за "" и "/" за да не се обръща /buildings/ към /{id} с празен id (NumberFormatException → ERR_INCOMPLETE_CHUNKED_ENCODING). */
    @GetMapping({"", "/"})
    public String buildingsPage(Model model) {
        try {
            java.util.Map<String, Object> data = buildingService.getBuildingsPageData();
            model.addAttribute("buildings", data.get("buildings"));
            model.addAttribute("buildingsCount", data.get("buildingsCount"));
            model.addAttribute("currentBuilding", data.get("currentBuilding"));
            model.addAttribute("apartmentsCount", data.get("apartmentsCount"));
            model.addAttribute("garagesCount", data.get("garagesCount"));
            model.addAttribute("basementsCount", data.get("basementsCount"));
            model.addAttribute("parkingSpacesCount", data.get("parkingSpacesCount"));
            model.addAttribute("commercialSpacesCount", data.get("commercialSpacesCount"));
            if (data.containsKey("error")) {
                model.addAttribute("error", "Грешка при зареждане: " + data.get("error"));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Грешка при зареждане на сградите: " + e.getMessage());
            model.addAttribute("buildings", java.util.Collections.emptyList());
            model.addAttribute("buildingsCount", 0);
            model.addAttribute("currentBuilding", null);
            model.addAttribute("apartmentsCount", 0);
            model.addAttribute("garagesCount", 0);
            model.addAttribute("basementsCount", 0);
            model.addAttribute("parkingSpacesCount", 0);
            model.addAttribute("commercialSpacesCount", 0);
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
            // Default stage for new buildings (user can change or clear it)
            building.setStage("Открита строителна площадка");
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
            // Set newly created building as current
            buildingService.setCurrentBuildingId(savedBuilding.getId());
            
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
    @Transactional(readOnly = true)
    public String viewBuilding(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null) {
            redirectAttributes.addFlashAttribute("error", "Невалиден идентификатор на сграда.");
            return "redirect:/buildings";
        }
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
    @Transactional(readOnly = true)
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

    @PostMapping("/set-current")
    public String setCurrentBuilding(@RequestParam("buildingId") Long id, RedirectAttributes redirectAttributes) {
        try {
            Building building = buildingService.getBuildingById(id)
                .orElseThrow(() -> new RuntimeException("Сграда с ID " + id + " не е намерена"));
            buildingService.setCurrentBuildingId(id);
            redirectAttributes.addFlashAttribute("success",
                "Избрана е сграда: " + building.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при избор на сграда: " + e.getMessage());
        }
        return "redirect:/buildings";
    }
    
    @PostMapping("/update-stage")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> updateBuildingStage(@RequestParam("buildingId") Long id, 
                                                               @RequestParam("stage") String stage) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Building building = buildingService.updateBuildingStage(id, stage);
            response.put("success", true);
            response.put("message", "Етапът на сграда \"" + building.getName() + "\" е променен на \"" + stage + "\". Всички обекти в сградата са актуализирани.");
            response.put("buildingId", building.getId());
            response.put("stage", building.getStage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при промяна на етап: " + e.getMessage());
        }
        return response;
    }
}



