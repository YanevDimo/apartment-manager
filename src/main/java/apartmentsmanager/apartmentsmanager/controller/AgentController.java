package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Agent;
import apartmentsmanager.apartmentsmanager.entity.Property;
import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.service.AgentService;
import apartmentsmanager.apartmentsmanager.service.InquiryService;
import apartmentsmanager.apartmentsmanager.service.PropertyService;
import apartmentsmanager.apartmentsmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/agent")
public class AgentController {
    
    private final AgentService agentService;
    private final PropertyService propertyService;
    private final InquiryService inquiryService;
    private final UserService userService;
    
    @Autowired
    public AgentController(AgentService agentService, PropertyService propertyService, 
                          InquiryService inquiryService, UserService userService) {
        this.agentService = agentService;
        this.propertyService = propertyService;
        this.inquiryService = inquiryService;
        this.userService = userService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // If agent doesn't exist, create one automatically
        Agent agent = agentService.getAgentByUserId(user.getId()).orElseGet(() -> {
            String defaultLicense = "AGENT-" + user.getId();
            return agentService.createAgentFromUser(user, defaultLicense);
        });
        
        List<Property> properties = propertyService.getPropertiesByAgent(agent.getId());
        List<apartmentsmanager.apartmentsmanager.entity.Inquiry> inquiries = 
                inquiryService.getInquiriesByAgentId(agent.getId());
        
        long pendingInquiries = inquiryService.getInquiriesByStatus(
                apartmentsmanager.apartmentsmanager.entity.Inquiry.InquiryStatus.PENDING).stream()
                .filter(inq -> inq.getProperty().getAgent().getId().equals(agent.getId()))
                .count();
        
        model.addAttribute("agent", agent);
        model.addAttribute("properties", properties);
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("pendingInquiries", pendingInquiries);
        model.addAttribute("totalProperties", properties != null ? properties.size() : 0);
        
        return "agent/dashboard";
    }
    
    @GetMapping("/properties")
    public String propertiesList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // If agent doesn't exist, create one automatically
        Agent agent = agentService.getAgentByUserId(user.getId()).orElseGet(() -> {
            String defaultLicense = "AGENT-" + user.getId();
            return agentService.createAgentFromUser(user, defaultLicense);
        });
        
        List<Property> properties = propertyService.getPropertiesByAgent(agent.getId());
        model.addAttribute("properties", properties);
        
        return "agent/properties";
    }
    
    @GetMapping("/properties/create")
    public String createPropertyForm(Model model) {
        model.addAttribute("property", new Property());
        return "agent/property-form";
    }
    
    @PostMapping("/properties/create")
    public String createProperty(@Valid @ModelAttribute Property property,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "agent/property-form";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // If agent doesn't exist, create one automatically
        Agent agent = agentService.getAgentByUserId(user.getId()).orElseGet(() -> {
            String defaultLicense = "AGENT-" + user.getId();
            return agentService.createAgentFromUser(user, defaultLicense);
        });
        
        property.setAgent(agent);
        property.setPublished(false); // Default to unpublished
        
        Property savedProperty = propertyService.saveProperty(property);
        
        // TODO: Handle image uploads
        
        redirectAttributes.addFlashAttribute("success", "Имотът е създаден успешно!");
        return "redirect:/agent/properties";
    }
    
    @GetMapping("/properties/edit/{id}")
    public String editPropertyForm(@PathVariable Long id, Model model) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
        
        model.addAttribute("property", property);
        return "agent/property-form";
    }
    
    @PostMapping("/properties/edit/{id}")
    public String updateProperty(@PathVariable Long id,
                                 @Valid @ModelAttribute Property property,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "agent/property-form";
        }
        
        Property existingProperty = propertyService.getPropertyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
        
        property.setId(id);
        property.setAgent(existingProperty.getAgent());
        
        propertyService.saveProperty(property);
        
        redirectAttributes.addFlashAttribute("success", "Имотът е обновен успешно!");
        return "redirect:/agent/properties";
    }
    
    @PostMapping("/properties/delete/{id}")
    public String deleteProperty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        propertyService.deleteProperty(id);
        redirectAttributes.addFlashAttribute("success", "Имотът е изтрит успешно!");
        return "redirect:/agent/properties";
    }
    
    @GetMapping("/inquiries")
    public String inquiriesList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // If agent doesn't exist, create one automatically
        Agent agent = agentService.getAgentByUserId(user.getId()).orElseGet(() -> {
            String defaultLicense = "AGENT-" + user.getId();
            return agentService.createAgentFromUser(user, defaultLicense);
        });
        
        List<apartmentsmanager.apartmentsmanager.entity.Inquiry> inquiries = 
                inquiryService.getInquiriesByAgentId(agent.getId());
        
        model.addAttribute("inquiries", inquiries);
        return "agent/inquiries";
    }
    
    @GetMapping("/inquiries/{id}")
    public String inquiryDetails(@PathVariable Long id, Model model) {
        apartmentsmanager.apartmentsmanager.entity.Inquiry inquiry = 
                inquiryService.getInquiryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + id));
        
        model.addAttribute("inquiry", inquiry);
        return "agent/inquiry-details";
    }
    
    @PostMapping("/inquiries/{id}/respond")
    public String respondToInquiry(@PathVariable Long id,
                                   @RequestParam String response,
                                   RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        inquiryService.respondToInquiry(id, response, user.getId());
        
        redirectAttributes.addFlashAttribute("success", "Отговорът е изпратен успешно!");
        return "redirect:/agent/inquiries/" + id;
    }
    
    @PostMapping("/inquiries/{id}/status")
    public String updateInquiryStatus(@PathVariable Long id,
                                      @RequestParam apartmentsmanager.apartmentsmanager.entity.Inquiry.InquiryStatus status,
                                      RedirectAttributes redirectAttributes) {
        inquiryService.updateInquiryStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Статусът е обновен успешно!");
        return "redirect:/agent/inquiries/" + id;
    }
    
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // If agent doesn't exist, create one automatically
        Agent agent = agentService.getAgentByUserId(user.getId()).orElseGet(() -> {
            String defaultLicense = "AGENT-" + user.getId();
            return agentService.createAgentFromUser(user, defaultLicense);
        });
        
        model.addAttribute("agent", agent);
        model.addAttribute("user", user);
        
        return "agent/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute Agent agent,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "agent/profile";
        }
        
        agentService.updateAgent(agent);
        redirectAttributes.addFlashAttribute("success", "Профилът е обновен успешно!");
        return "redirect:/agent/profile";
    }
}

