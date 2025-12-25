package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
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
    
    @Autowired
    public ExcelServiceImpl(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
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
                    Apartment apartment = new Apartment();
                    
                    // Read data from row (adjust column indices based on export format)
                    if (row.getCell(1) != null) {
                        apartment.setBuildingName(getCellValueAsString(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        apartment.setApartmentNumber(getCellValueAsString(row.getCell(2)));
                    }
                    if (row.getCell(3) != null) {
                        apartment.setArea(BigDecimal.valueOf(getCellValueAsDouble(row.getCell(3))));
                    }
                    if (row.getCell(4) != null) {
                        apartment.setPricePerM2(BigDecimal.valueOf(getCellValueAsDouble(row.getCell(4))));
                    }
                    if (row.getCell(6) != null) {
                        apartment.setStage(getCellValueAsString(row.getCell(6)));
                    }
                    if (row.getCell(10) != null) {
                        apartment.setNotes(getCellValueAsString(row.getCell(10)));
                    }
                    
                    apartment.setIsSold(true);
                    
                    // Validate required fields
                    if (apartment.getBuildingName() == null || apartment.getBuildingName().isEmpty() ||
                        apartment.getApartmentNumber() == null || apartment.getApartmentNumber().isEmpty()) {
                        skipped++;
                        errors.add("Ред " + (row.getRowNum() + 1) + ": Липсват задължителни полета");
                        continue;
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
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
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
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
}


