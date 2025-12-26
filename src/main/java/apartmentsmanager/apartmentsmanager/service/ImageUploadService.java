package apartmentsmanager.apartmentsmanager.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageUploadService {
    
    String uploadImage(MultipartFile file, String directory) throws IOException;
    
    String uploadPropertyImage(MultipartFile file, Long propertyId) throws IOException;
    
    List<String> uploadMultipleImages(List<MultipartFile> files, String directory) throws IOException;
    
    List<String> uploadMultiplePropertyImages(List<MultipartFile> files, Long propertyId) throws IOException;
    
    void deleteImage(String imagePath) throws IOException;
    
    String generateThumbnail(String imagePath) throws IOException;
    
    boolean isValidImageFile(MultipartFile file);
    
    String getImageDirectory(Long propertyId);
}




