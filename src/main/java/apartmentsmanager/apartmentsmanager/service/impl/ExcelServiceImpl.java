package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import apartmentsmanager.apartmentsmanager.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelServiceImpl implements ExcelService {
    
    private final ApartmentService apartmentService;
    private final BuildingService buildingService;
    private final ClientService clientService;
    
    @Autowired
    public ExcelServiceImpl(ApartmentService apartmentService, 
                           BuildingService buildingService,
                           ClientService clientService) {
        this.apartmentService = apartmentService;
        this.buildingService = buildingService;
        this.clientService = clientService;
    }
    
    @Override
    public byte[] exportApartmentsToExcel(List<Apartment> apartments) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Апартаменти");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Сграда", "Апартамент", "Площ (кв.м)", "Цена/кв.м (€)", 
                "Обща цена (€)", "Етап", "Клиент", "Платено (€)", "Остатък (€)", "Бележки"
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Apartment apt : apartments) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(apt.getId() != null ? apt.getId() : 0);
                row.createCell(1).setCellValue(apt.getBuildingName() != null ? apt.getBuildingName() : "");
                row.createCell(2).setCellValue(apt.getApartmentNumber() != null ? apt.getApartmentNumber() : "");
                row.createCell(3).setCellValue(apt.getArea() != null ? apt.getArea().doubleValue() : 0);
                row.createCell(4).setCellValue(apt.getPricePerM2() != null ? apt.getPricePerM2().doubleValue() : 0);
                row.createCell(5).setCellValue(apt.getTotalPrice() != null ? apt.getTotalPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(apt.getStage() != null ? apt.getStage() : "");
                row.createCell(7).setCellValue(apt.getClient() != null && apt.getClient().getName() != null ? apt.getClient().getName() : "");
                row.createCell(8).setCellValue(apt.getTotalPaid() != null ? apt.getTotalPaid().doubleValue() : 0);
                row.createCell(9).setCellValue(apt.getRemainingPayment() != null ? apt.getRemainingPayment().doubleValue() : 0);
                row.createCell(10).setCellValue(apt.getNotes() != null ? apt.getNotes() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Грешка при експорт на Excel: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> importApartmentsFromExcel(MultipartFile file) {
        return importApartmentsFromExcelForBuilding(file, null);
    }

    @Override
    public Map<String, Object> importApartmentsFromExcelForBuilding(MultipartFile file, Long buildingId) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        Building currentBuilding = null;
        if (buildingId != null) {
            currentBuilding = buildingService.getBuildingById(buildingId).orElse(null);
        }
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Skip empty rows
                if (row == null || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }
                
                try {
                    Apartment apartment = new Apartment();
                    
                    // Read data from row (adjust column indices based on export format)
                    if (currentBuilding != null) {
                        apartment.setBuilding(currentBuilding);
                        apartment.setBuildingName(currentBuilding.getName());
                    } else if (row.getCell(1) != null) {
                        apartment.setBuildingName(getCellValueAsString(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        apartment.setApartmentNumber(getCellValueAsString(row.getCell(2)));
                    }
                    
                    // Read area (column 3) - required field
                    BigDecimal area = null;
                    if (row.getCell(3) != null) {
                        double areaValue = getCellValueAsDouble(row.getCell(3));
                        if (areaValue > 0) {
                            area = BigDecimal.valueOf(areaValue);
                        }
                    }
                    apartment.setArea(area);
                    
                    // Read pricePerM2 (column 4) - optional
                    BigDecimal pricePerM2 = null;
                    if (row.getCell(4) != null) {
                        double priceValue = getCellValueAsDouble(row.getCell(4));
                        if (priceValue > 0) {
                            pricePerM2 = BigDecimal.valueOf(priceValue);
                        }
                    }
                    apartment.setPricePerM2(pricePerM2);
                    
                    if (row.getCell(6) != null) {
                        apartment.setStage(getCellValueAsString(row.getCell(6)));
                    }
                    if (row.getCell(10) != null) {
                        apartment.setNotes(getCellValueAsString(row.getCell(10)));
                    }
                    
                    apartment.setIsSold(true);
                    
                    // Validate required fields
                    if ((apartment.getBuildingName() == null || apartment.getBuildingName().trim().isEmpty()) ||
                        apartment.getApartmentNumber() == null || apartment.getApartmentNumber().trim().isEmpty()) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Липсват задължителни полета");
                        continue;
                    }
                    
                    // Validate area
                    if (apartment.getArea() == null || apartment.getArea().compareTo(BigDecimal.valueOf(0.01)) < 0) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Площта трябва да е по-голяма от 0");
                        continue;
                    }
                    
                    // Resolve building if not provided
                    if (apartment.getBuilding() == null) {
                        Building foundBuilding = buildingService.getBuildingByName(apartment.getBuildingName()).orElse(null);
                        if (foundBuilding == null) {
                            // Create building if missing
                            Building newBuilding = new Building();
                            newBuilding.setName(apartment.getBuildingName());
                            newBuilding.setStatus("активна");
                            foundBuilding = buildingService.saveBuilding(newBuilding);
                        }
                        apartment.setBuilding(foundBuilding);
                        apartment.setBuildingName(foundBuilding.getName());
                    }

                    // Check for duplicates
                    if (apartmentService.apartmentExists(apartment.getBuildingName(),
                                                        apartment.getApartmentNumber(), null)) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Апартамент вече съществува");
                        continue;
                    }
                    
                    // Save apartment
                    apartmentService.saveApartment(apartment);
                    imported++;
                    
                } catch (Exception e) {
                    skipped++;
                    String errorMessage = e.getMessage();
                    // Extract validation error message if it's a constraint violation
                    if (errorMessage != null && errorMessage.contains("ConstraintViolationImpl")) {
                        if (errorMessage.contains("area")) {
                            errors.add("Ред " + (row.getRowNum() + 1) + ": Площта трябва да е по-голяма от 0");
                        } else {
                            errors.add("Ред " + (row.getRowNum() + 1) + ": " + errorMessage);
                        }
                    } else {
                        errors.add("Ред " + (row.getRowNum() + 1) + ": " + errorMessage);
                    }
                }
            }
            
        } catch (Exception e) {
            errors.add("Грешка при четене на файла: " + e.getMessage());
        }
        
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        
        return result;
    }
    
    @Override
    public boolean validateExcelStructure(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return false;
            }
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return false;
            }
            
            // Check if required columns exist (at least some basic validation)
            int cellCount = headerRow.getPhysicalNumberOfCells();
            return cellCount >= 3; // At least 3 columns expected
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // Check if it's a whole number or decimal
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // Try to get the formula result
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case STRING:
                                return cell.getStringCellValue().trim();
                            case NUMERIC:
                                double formulaValue = cell.getNumericCellValue();
                                if (formulaValue == (long) formulaValue) {
                                    return String.valueOf((long) formulaValue);
                                } else {
                                    return String.valueOf(formulaValue);
                                }
                            default:
                                return cell.getCellFormula();
                        }
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                case BLANK:
                    return "";
                default:
                    return "";
            }
        } catch (Exception e) {
            System.err.println("Грешка при четене на клетка: " + e.getMessage());
            return "";
        }
    }
    
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String strValue = cell.getStringCellValue().trim();
                if (strValue.isEmpty()) {
                    return 0.0;
                }
                try {
                    // Handle comma as decimal separator (Bulgarian format)
                    strValue = strValue.replace(',', '.');
                    return Double.parseDouble(strValue);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case BLANK:
                return 0.0;
            case FORMULA:
                try {
                    switch (cell.getCachedFormulaResultType()) {
                        case NUMERIC:
                            return cell.getNumericCellValue();
                        case STRING:
                            String formulaStr = cell.getStringCellValue().trim();
                            if (formulaStr.isEmpty()) {
                                return 0.0;
                            }
                            formulaStr = formulaStr.replace(',', '.');
                            return Double.parseDouble(formulaStr);
                        default:
                            return 0.0;
                    }
                } catch (Exception e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
    
    @Override
    public Map<String, Object> importBuildingsFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Skip empty rows
                if (row == null || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }
                
                try {
                    Building building = new Building();
                    
                    // Expected columns: Име, Адрес, Статус, Етап, Бележки
                    String name = "";
                    if (row.getCell(0) != null) {
                        name = getCellValueAsString(row.getCell(0)).trim();
                        building.setName(name);
                    }
                    
                    if (row.getCell(1) != null) {
                        String address = getCellValueAsString(row.getCell(1)).trim();
                        if (!address.isEmpty()) {
                            building.setAddress(address);
                        }
                    }
                    
                    if (row.getCell(2) != null) {
                        String status = getCellValueAsString(row.getCell(2)).trim();
                        if (!status.isEmpty()) {
                            building.setStatus(status);
                        }
                    }
                    
                    if (row.getCell(3) != null) {
                        String stage = getCellValueAsString(row.getCell(3)).trim();
                        if (!stage.isEmpty()) {
                            building.setStage(stage);
                        }
                    }
                    
                    if (row.getCell(4) != null) {
                        String notes = getCellValueAsString(row.getCell(4)).trim();
                        if (!notes.isEmpty()) {
                            building.setNotes(notes);
                        }
                    }
                    
                    // Skip rows with empty name (completely empty rows)
                    if (name == null || name.isEmpty()) {
                        continue; // Skip empty rows silently
                    }
                    
                    // Validate required fields
                    if (building.getName() == null || building.getName().isEmpty()) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Липсва име на сградата");
                        continue;
                    }
                    
                    // Check for duplicates
                    if (buildingService.buildingExists(building.getName())) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Сграда '" + building.getName() + "' вече съществува");
                        continue;
                    }
                    
                    // Set default status if not provided
                    if (building.getStatus() == null || building.getStatus().isEmpty()) {
                        building.setStatus("активна");
                    }
                    
                    // Initialize lists before saving
                    building.setApartments(new java.util.ArrayList<>());
                    building.setGarages(new java.util.ArrayList<>());
                    building.setBasements(new java.util.ArrayList<>());
                    building.setParkingSpaces(new java.util.ArrayList<>());
                    building.setCommercialSpaces(new java.util.ArrayList<>());
                    
                    // Save building
                    Building savedBuilding = buildingService.saveBuilding(building);
                    imported++;
                    
                    System.out.println("✓ Импортирана сграда: " + savedBuilding.getName() + " (ID: " + savedBuilding.getId() + ")");
                    
                } catch (Exception e) {
                    skipped++;
                    String errorMsg = "Ред " + (row.getRowNum() + 1) + ": " + e.getMessage();
                    errors.add(errorMsg);
                    System.err.println("✗ Грешка при импорт на ред " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("=== Импорт на сгради завършен: " + imported + " импортирани, " + skipped + " пропуснати ===");
            
        } catch (Exception e) {
            String errorMsg = "Грешка при четене на файла: " + e.getMessage();
            errors.add(errorMsg);
            System.err.println("✗ " + errorMsg);
            e.printStackTrace();
        }
        
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        
        System.out.println("Резултат от импорт: " + result);
        
        return result;
    }
    
    @Override
    public Map<String, Object> importClientsFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                try {
                    Client client = new Client();
                    
                    // Expected columns: Име, Телефон, Email, Адрес, Бележки
                    if (row.getCell(0) != null) {
                        client.setName(getCellValueAsString(row.getCell(0)).trim());
                    }
                    if (row.getCell(1) != null) {
                        client.setPhone(getCellValueAsString(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        client.setEmail(getCellValueAsString(row.getCell(2)));
                    }
                    if (row.getCell(3) != null) {
                        client.setAddress(getCellValueAsString(row.getCell(3)));
                    }
                    if (row.getCell(4) != null) {
                        client.setNotes(getCellValueAsString(row.getCell(4)));
                    }
                    
                    // Validate required fields
                    if (client.getName() == null || client.getName().isEmpty()) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Липсва име на клиента");
                        continue;
                    }
                    
                    // Save client
                    clientService.saveClient(client);
                    imported++;
                    
                } catch (Exception e) {
                    skipped++;
                    errors.add("Ред " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            errors.add("Грешка при четене на файла: " + e.getMessage());
        }
        
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        
        return result;
    }
    
    @Override
    public byte[] generateBuildingsTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Сгради");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Име", "Адрес", "Статус", "Етап", "Бележки"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add example row
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Сграда А");
            exampleRow.createCell(1).setCellValue("ул. Примерна 123, София");
            exampleRow.createCell(2).setCellValue("активна");
            exampleRow.createCell(3).setCellValue("Акт 14");
            exampleRow.createCell(4).setCellValue("Примерни бележки");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Грешка при генериране на шаблон за сгради: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] generateClientsTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Клиенти");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Име", "Телефон", "Email", "Адрес", "Бележки"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add example row
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Иван Петров");
            exampleRow.createCell(1).setCellValue("0888123456");
            exampleRow.createCell(2).setCellValue("ivan@example.com");
            exampleRow.createCell(3).setCellValue("София, ул. Примерна 1");
            exampleRow.createCell(4).setCellValue("Примерни бележки");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Грешка при генериране на шаблон за клиенти: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] generateApartmentsTemplate() {
        // Use the same format as export
        return exportApartmentsToExcel(List.of());
    }
}



