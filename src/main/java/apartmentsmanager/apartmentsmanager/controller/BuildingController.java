package apartmentsmanager.apartmentsmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/buildings")
public class BuildingController {
    
    @GetMapping
    public String buildingsPage() {
        return "buildings";
    }
    
    @GetMapping("/add")
    public String addBuildingPage() {
        return "add_building";
    }
}



