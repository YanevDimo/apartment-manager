package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/buildings")
public class BuildingController {
    
    private final BuildingService buildingService;
    
    @Autowired
    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }
    
    @GetMapping
    public String buildingsPage(Model model) {
        try {
            model.addAttribute("buildings", buildingService.getAllBuildings());
        } catch (Exception e) {
            model.addAttribute("error", "Грешка при зареждане на сградите: " + e.getMessage());
            model.addAttribute("buildings", java.util.Collections.emptyList());
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
}



