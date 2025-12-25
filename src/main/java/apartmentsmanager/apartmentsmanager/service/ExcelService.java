package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ExcelService {
    
    /**
     * Export all apartments to Excel
     */
    byte[] exportApartmentsToExcel(List<Apartment> apartments);
    
    /**
     * Import apartments from Excel file
     */
    Map<String, Object> importApartmentsFromExcel(MultipartFile file);
    
    /**
     * Validate Excel file structure
     */
    boolean validateExcelStructure(InputStream inputStream);
}

