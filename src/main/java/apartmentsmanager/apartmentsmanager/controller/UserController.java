package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Inquiry;
import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.service.InquiryService;
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

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;
    private final InquiryService inquiryService;
    
    @Autowired
    public UserController(UserService userService, InquiryService inquiryService) {
        this.userService = userService;
        this.inquiryService = inquiryService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        List<Inquiry> inquiries = inquiryService.getInquiriesByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("inquiries", inquiries);
        
        return "user/dashboard";
    }
    
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        model.addAttribute("user", user);
        return "user/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/profile";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Preserve password if not changed
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(null); // Will be preserved in service
        }
        
        user.setId(currentUser.getId());
        user.setRole(currentUser.getRole()); // Prevent role change
        user.setActive(currentUser.getActive()); // Prevent active status change
        
        userService.updateUser(user);
        
        redirectAttributes.addFlashAttribute("success", "Профилът е обновен успешно!");
        return "redirect:/user/profile";
    }
    
    @GetMapping("/inquiries")
    public String inquiryHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        List<Inquiry> inquiries = inquiryService.getInquiriesByUserId(user.getId());
        
        model.addAttribute("inquiries", inquiries);
        return "user/inquiry-history";
    }
    
    @GetMapping("/inquiries/{id}")
    public String inquiryDetails(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        Inquiry inquiry = inquiryService.getInquiryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + id));
        
        // Check if inquiry belongs to user
        if (inquiry.getUser() == null || !inquiry.getUser().getId().equals(user.getId())) {
            return "redirect:/user/inquiries";
        }
        
        model.addAttribute("inquiry", inquiry);
        return "user/inquiry-details";
    }
}



