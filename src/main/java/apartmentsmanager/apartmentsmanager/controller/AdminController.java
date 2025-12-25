package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Inquiry;
import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.service.AgentService;
import apartmentsmanager.apartmentsmanager.service.InquiryService;
import apartmentsmanager.apartmentsmanager.service.PropertyService;
import apartmentsmanager.apartmentsmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final PropertyService propertyService;
    private final InquiryService inquiryService;
    private final AgentService agentService;
    
    @Autowired
    public AdminController(UserService userService, PropertyService propertyService, 
                          InquiryService inquiryService, AgentService agentService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.inquiryService = inquiryService;
        this.agentService = agentService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userService.getAllUsers().size();
        long totalAgents = userService.getUsersByRole(User.Role.AGENT).size();
        long totalProperties = propertyService.getAllProperties().size();
        long publishedProperties = propertyService.countPublishedProperties();
        long totalInquiries = inquiryService.getAllInquiries().size();
        long pendingInquiries = inquiryService.countInquiriesByStatus(Inquiry.InquiryStatus.PENDING);
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAgents", totalAgents);
        model.addAttribute("totalProperties", totalProperties);
        model.addAttribute("publishedProperties", publishedProperties);
        model.addAttribute("totalInquiries", totalInquiries);
        model.addAttribute("pendingInquiries", pendingInquiries);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String usersList(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @GetMapping("/users/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        model.addAttribute("user", user);
        return "admin/user-details";
    }
    
    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.activateUser(id);
        redirectAttributes.addFlashAttribute("success", "Потребителят е активиран успешно!");
        return "redirect:/admin/users/" + id;
    }
    
    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deactivateUser(id);
        redirectAttributes.addFlashAttribute("success", "Потребителят е деактивиран успешно!");
        return "redirect:/admin/users/" + id;
    }
    
    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id,
                                 @RequestParam String newRole,
                                 RedirectAttributes redirectAttributes) {
        User.Role role = User.Role.valueOf(newRole.toUpperCase());
        userService.changeUserRole(id, role);
        redirectAttributes.addFlashAttribute("success", "Ролята е променена успешно!");
        return "redirect:/admin/users/" + id;
    }
    
    @GetMapping("/inquiries")
    public String inquiriesList(Model model) {
        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        model.addAttribute("inquiries", inquiries);
        return "admin/inquiries";
    }
    
    @GetMapping("/inquiries/{id}")
    public String inquiryDetails(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryService.getInquiryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + id));
        
        model.addAttribute("inquiry", inquiry);
        return "admin/inquiry-details";
    }
    
    @GetMapping("/system")
    public String systemHealth(Model model) {
        // System health information
        Map<String, Object> systemInfo = Map.of(
            "javaVersion", System.getProperty("java.version"),
            "osName", System.getProperty("os.name"),
            "osVersion", System.getProperty("os.version"),
            "totalMemory", Runtime.getRuntime().totalMemory(),
            "freeMemory", Runtime.getRuntime().freeMemory(),
            "maxMemory", Runtime.getRuntime().maxMemory()
        );
        
        model.addAttribute("systemInfo", systemInfo);
        return "admin/system-health";
    }
}

