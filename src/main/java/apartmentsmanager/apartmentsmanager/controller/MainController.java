package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class MainController {
    
    private final StatisticsService statisticsService;
    private final ApartmentService apartmentService;
    
    @Autowired
    public MainController(StatisticsService statisticsService, ApartmentService apartmentService) {
        this.statisticsService = statisticsService;
        this.apartmentService = apartmentService;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        Map<String, Object> stats = statisticsService.getStatistics();
        
        // Add additional data for dashboard
        long overdueCount = apartmentService.getApartmentsWithOverduePayments().size();
        stats.put("overdueCount", overdueCount);
        
        model.addAttribute("statistics", stats);
        return "index";
    }
    
    @GetMapping("/index")
    public String indexRedirect() {
        return "redirect:/";
    }
    
    @GetMapping("/api/statistics")
    @ResponseBody
    public Map<String, Object> getStatisticsApi() {
        Map<String, Object> stats = statisticsService.getStatistics();
        long overdueCount = apartmentService.getApartmentsWithOverduePayments().size();
        stats.put("overdueCount", overdueCount);
        return stats;
    }
}
