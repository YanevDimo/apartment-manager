package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller

@RequestMapping("/excel")
public class ExcelController {
    
    private final ExcelService excelService;
    private final ApartmentService apartmentService;
    
    @Autowired
    public ExcelController(ExcelService excelService, ApartmentService apartmentService) {
        this.excelService = excelService;
        this.apartmentService = apartmentService;
    }
    
    @GetMapping
    public String excelPage(Model model) {
        return "import_excel";
    }
    
    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @ResponseBody
    public ResponseEntity<byte[]> exportApartmentsToExcel() throws IOException {
        try {
            List<Apartment> apartments = apartmentService.getAllSoldApartments();
            if (apartments == null || apartments.isEmpty()) {
                // Return empty Excel file if no apartments
                apartments = List.of();
            }
            byte[] excelData = excelService.exportApartmentsToExcel(apartments);
            
            HttpHeaders headers = new HttpHeaders();
            // Set proper Excel MIME type
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            // Force download - use setContentDispositionFormData which properly sets the header
            headers.setContentDispositionFormData("attachment", "apartments_export.xlsx");
            headers.setContentLength(excelData.length);
            // Prevent caching
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            // Ensure binary data is not corrupted
            headers.add("Content-Transfer-Encoding", "binary");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importDataFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "apartments") String importType) {
        Map<String, Object> response = new HashMap<>();
        
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Файлът е празен");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
            response.put("success", false);
            response.put("message", "Поддържат се само Excel файлове (.xlsx, .xls)");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // For buildings import, skip strict validation as format is simpler
            if (!"buildings".equalsIgnoreCase(importType)) {
                // Validate file structure for apartments and clients
                ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes());
                if (!excelService.validateExcelStructure(inputStream)) {
                    response.put("success", false);
                    response.put("message", "Невалидна структура на Excel файла. Моля, използвайте предоставения шаблон.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Import based on type
            Map<String, Object> importResult;
            switch (importType.toLowerCase()) {
                case "buildings":
                    importResult = excelService.importBuildingsFromExcel(file);
                    break;
                case "clients":
                    importResult = excelService.importClientsFromExcel(file);
                    break;
                case "apartments":
                default:
                    importResult = excelService.importApartmentsFromExcel(file);
                    break;
            }
            
            response.put("success", true);
            response.put("message", "Импортът е завършен успешно");
            response.put("imported", importResult.get("imported"));
            response.put("errors", importResult.get("errors"));
            response.put("skipped", importResult.get("skipped"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Грешка при импорт: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam(value = "type", defaultValue = "apartments") String templateType) {
        try {
            byte[] templateData;
            String filename;
            
            switch (templateType.toLowerCase()) {
                case "buildings":
                    templateData = excelService.generateBuildingsTemplate();
                    filename = "buildings_template.xlsx";
                    break;
                case "clients":
                    templateData = excelService.generateClientsTemplate();
                    filename = "clients_template.xlsx";
                    break;
                case "apartments":
                default:
                    templateData = excelService.generateApartmentsTemplate();
                    filename = "apartments_template.xlsx";
                    break;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(templateData.length);
            
            return new ResponseEntity<>(templateData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

