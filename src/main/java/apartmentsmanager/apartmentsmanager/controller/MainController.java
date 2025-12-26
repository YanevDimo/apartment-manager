package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {
    
    private final ClientService clientService;
    
    @Autowired
    public MainController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    @GetMapping("/")
    public String index() {
        // Redirect to dashboard for admin, public home for others
        return "redirect:/index";
    }
    
    @GetMapping("/index")
    public String indexPage() {
        return "dashboard";
    }
    
    @GetMapping("/add-apartment")
    public String addApartmentPage() {
        return "add_apartment";
    }
    
    @GetMapping("/add-building")
    public String addBuildingPage() {
        return "add_building";
    }
    
    @GetMapping("/add-client")
    public String addClientPage(Model model) {
        model.addAttribute("client", new Client());
        return "add_client";
    }
    
    @PostMapping("/add-client")
    public String addClient(@Valid @ModelAttribute Client client,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "add_client";
        }
        
        // Check for duplicate EGN if provided
        if (client.getEgn() != null && !client.getEgn().trim().isEmpty()) {
            if (clientService.findByEgn(client.getEgn()).isPresent()) {
                bindingResult.rejectValue("egn", "error.egn", "Клиент с този ЕГН/ЕИК вече съществува");
                return "add_client";
            }
        }
        
        try {
            clientService.saveClient(client);
            redirectAttributes.addFlashAttribute("success", "Клиентът е добавен успешно!");
            return "redirect:/clients";
        } catch (Exception e) {
            bindingResult.reject("error.client", "Грешка при добавяне на клиент: " + e.getMessage());
            return "add_client";
        }
    }
}
