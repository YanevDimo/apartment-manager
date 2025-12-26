package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clients")
public class ClientController {
    
    private final ClientService clientService;
    
    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    @GetMapping
    public String clientsPage(@RequestParam(required = false) String search, Model model) {
        List<Client> clients;
        if (search != null && !search.trim().isEmpty()) {
            clients = clientService.searchClients(search.trim());
            model.addAttribute("searchTerm", search);
        } else {
            clients = clientService.getAllClients();
        }
        model.addAttribute("clients", clients);
        return "clients";
    }
    
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllClientsApi() {
        List<Client> clients = clientService.getAllClients();
        
        List<Map<String, Object>> clientData = clients.stream().map(client -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", client.getId());
            data.put("name", client.getName());
            data.put("phone", client.getPhone());
            data.put("email", client.getEmail());
            data.put("egn", client.getEgn());
            data.put("address", client.getAddress());
            data.put("notes", client.getNotes());
            data.put("apartmentsCount", client.getApartments() != null ? client.getApartments().size() : 0);
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("clients", clientData);
        response.put("total", clients.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchClientsApi(@RequestParam(required = false) String q) {
        List<Client> clients = clientService.searchClients(q);
        
        List<Map<String, Object>> clientData = clients.stream().map(client -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", client.getId());
            data.put("name", client.getName());
            data.put("phone", client.getPhone());
            data.put("email", client.getEmail());
            data.put("egn", client.getEgn());
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("clients", clientData);
        response.put("total", clients.size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addClient(@Valid @RequestBody Client client,
                                                         BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Грешка при валидация на данните");
            response.put("errors", bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check for duplicate EGN if provided
        if (client.getEgn() != null && !client.getEgn().trim().isEmpty()) {
            if (clientService.findByEgn(client.getEgn()).isPresent()) {
                response.put("success", false);
                response.put("message", "Клиент с този ЕГН/ЕИК вече съществува");
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        try {
            Client saved = clientService.saveClient(client);
            response.put("success", true);
            response.put("message", "Клиентът е добавен успешно");
            response.put("client", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при добавяне на клиент: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateClient(@PathVariable Long id,
                                                           @Valid @RequestBody Client client,
                                                           BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Грешка при валидация на данните");
            response.put("errors", bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(response);
        }
        
        Client existingClient = clientService.getClientById(id).orElse(null);
        if (existingClient == null) {
            response.put("success", false);
            response.put("message", "Клиентът не е намерен");
            return ResponseEntity.notFound().build();
        }
        
        // Check for duplicate EGN if provided (excluding current client)
        if (client.getEgn() != null && !client.getEgn().trim().isEmpty()) {
            clientService.findByEgn(client.getEgn()).ifPresent(found -> {
                if (!found.getId().equals(id)) {
                    response.put("success", false);
                    response.put("message", "Клиент с този ЕГН/ЕИК вече съществува");
                }
            });
            
            if (response.containsKey("success") && !(Boolean) response.get("success")) {
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        client.setId(id);
        
        try {
            Client saved = clientService.saveClient(client);
            response.put("success", true);
            response.put("message", "Клиентът е обновен успешно");
            response.put("client", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при обновяване на клиент: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Client client = clientService.getClientById(id).orElse(null);
        if (client == null) {
            response.put("success", false);
            response.put("message", "Клиентът не е намерен");
            return ResponseEntity.notFound().build();
        }
        
        // Check if client has apartments
        if (client.getApartments() != null && !client.getApartments().isEmpty()) {
            response.put("success", false);
            response.put("message", "Не можете да изтриете клиент с асоциирани апартаменти");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            clientService.deleteClient(id);
            response.put("success", true);
            response.put("message", "Клиентът е изтрит успешно");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Грешка при изтриване: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

