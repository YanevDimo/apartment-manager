package apartmentsmanager.apartmentsmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    @GetMapping("/")
    public String index() {
        // Redirect to dashboard for admin, public home for others
        return "redirect:/index";
    }
    
    @GetMapping("/index")
    public String indexPage() {
        return "index";
    }
    
    @GetMapping("/add-apartment")
    public String addApartmentPage() {
        return "add_apartment";
    }
    
    @GetMapping("/add-building")
    public String addBuildingPage() {
        return "add_building";
    }
}
