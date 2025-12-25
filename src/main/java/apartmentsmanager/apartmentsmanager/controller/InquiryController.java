package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Inquiry;
import apartmentsmanager.apartmentsmanager.entity.Property;
import apartmentsmanager.apartmentsmanager.entity.User;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inquiries")
public class InquiryController {
    
    private final InquiryService inquiryService;
    private final PropertyService propertyService;
    private final UserService userService;
    
    @Autowired
    public InquiryController(InquiryService inquiryService, PropertyService propertyService, 
                            UserService userService) {
        this.inquiryService = inquiryService;
        this.propertyService = propertyService;
        this.userService = userService;
    }
    
    @PostMapping("/submit")
    public String submitInquiry(@RequestParam Long propertyId,
                                @Valid @ModelAttribute Inquiry inquiry,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Моля, попълнете всички задължителни полета.");
            return "redirect:/public/properties/" + propertyId;
        }
        
        Property property = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        
        if (!property.getPublished()) {
            redirectAttributes.addFlashAttribute("error", "Имотът не е публикуван.");
            return "redirect:/public/properties";
        }
        
        // Set property
        inquiry.setProperty(property);
        
        // Set user if authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userService.getUserByUsername(auth.getName()).orElse(null);
            inquiry.setUser(user);
        }
        
        // Set default status
        inquiry.setStatus(Inquiry.InquiryStatus.PENDING);
        
        inquiryService.saveInquiry(inquiry);
        
        redirectAttributes.addFlashAttribute("success", 
                "Вашето запитване е изпратено успешно! Ще се свържем с вас скоро.");
        return "redirect:/public/properties/" + propertyId;
    }
    
    @GetMapping("/my")
    public String myInquiries(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }
        
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + auth.getName()));
        
        var inquiries = inquiryService.getInquiriesByUserId(user.getId());
        model.addAttribute("inquiries", inquiries);
        
        return "user/inquiry-history";
    }
}


