package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/contract")
public class ContractController {
    
    private final ContractService contractService;
    private final ApartmentService apartmentService;
    
    @Autowired
    public ContractController(ContractService contractService, ApartmentService apartmentService) {
        this.contractService = contractService;
        this.apartmentService = apartmentService;
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long id) {
        Apartment apartment = apartmentService.getApartmentById(id).orElse(null);
        
        if (apartment == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            byte[] contractData = contractService.generateContract(apartment);
            String filename = contractService.getContractFilename(apartment);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(contractData.length);
            
            return new ResponseEntity<>(contractData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/preview/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<String> previewContract(@PathVariable Long id) {
        Apartment apartment = apartmentService.getApartmentById(id).orElse(null);
        
        if (apartment == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            byte[] contractData = contractService.generateContract(apartment);
            String contractText = new String(contractData, StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            
            return new ResponseEntity<>(contractText, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}




