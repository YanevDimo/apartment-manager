package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user, 
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        try {
            // Check if username exists
            if (userService.usernameExists(user.getUsername())) {
                bindingResult.rejectValue("username", "error.username", "Потребителското име вече съществува");
                return "register";
            }
            
            // Check if email exists
            if (userService.emailExists(user.getEmail())) {
                bindingResult.rejectValue("email", "error.email", "Email адресът вече съществува");
                return "register";
            }
            
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Регистрацията е успешна! Моля, влезте в системата.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            bindingResult.reject("error.registration", "Грешка при регистрация: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}

