package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Property;
import apartmentsmanager.apartmentsmanager.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/public")
public class PublicController {
    
    private final PropertyService propertyService;
    
    @Autowired
    public PublicController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }
    
    @GetMapping("/home")
    public String home(Model model) {
        List<Property> featuredProperties = propertyService.getFeaturedProperties();
        List<Property> recentProperties = propertyService.getRecentProperties(6);
        model.addAttribute("featuredProperties", featuredProperties);
        model.addAttribute("recentProperties", recentProperties);
        return "public/home";
    }
    
    @GetMapping("/properties")
    public String propertiesList(@RequestParam(required = false) String city,
                                 @RequestParam(required = false) Property.PropertyType type,
                                 @RequestParam(required = false) BigDecimal minPrice,
                                 @RequestParam(required = false) BigDecimal maxPrice,
                                 Model model) {
        List<Property> properties = propertyService.searchProperties(city, type, minPrice, maxPrice);
        List<String> cities = propertyService.getAllCities();
        
        model.addAttribute("properties", properties);
        model.addAttribute("cities", cities);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        
        return "public/property-list";
    }
    
    @GetMapping("/properties/{id}")
    public String propertyDetails(@PathVariable Long id, Model model) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
        
        if (!property.getPublished()) {
            return "redirect:/public/properties";
        }
        
        model.addAttribute("property", property);
        return "public/property-details";
    }
    
    @GetMapping("/about")
    public String about() {
        return "public/about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "public/contact";
    }
    
    @PostMapping("/contact")
    public String submitContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String subject,
                                @RequestParam String message,
                                RedirectAttributes redirectAttributes) {
        // TODO: Implement email sending or save to database
        redirectAttributes.addFlashAttribute("success", "Съобщението е изпратено успешно! Ще се свържем с вас скоро.");
        return "redirect:/public/contact";
    }
}

