package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDirectory;
    
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @Override
    public String uploadImage(MultipartFile file, String directory) throws IOException {
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }
        
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadDirectory, directory);
        
        // Create directory if it doesn't exist
        Files.createDirectories(uploadPath);
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/" + directory + "/" + fileName;
    }
    
    @Override
    public String uploadPropertyImage(MultipartFile file, Long propertyId) throws IOException {
        String directory = getImageDirectory(propertyId);
        return uploadImage(file, directory);
    }
    
    @Override
    public List<String> uploadMultipleImages(List<MultipartFile> files, String directory) throws IOException {
        List<String> uploadedPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty() && isValidImageFile(file)) {
                String path = uploadImage(file, directory);
                uploadedPaths.add(path);
            }
        }
        return uploadedPaths;
    }
    
    @Override
    public List<String> uploadMultiplePropertyImages(List<MultipartFile> files, Long propertyId) throws IOException {
        String directory = getImageDirectory(propertyId);
        return uploadMultipleImages(files, directory);
    }
    
    @Override
    public void deleteImage(String imagePath) throws IOException {
        Path filePath = Paths.get(uploadDirectory, imagePath.replaceFirst("/", ""));
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
    
    @Override
    public String generateThumbnail(String imagePath) throws IOException {
        // TODO: Implement thumbnail generation using image processing library
        // For now, return the original path
        return imagePath;
    }
    
    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return false;
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    @Override
    public String getImageDirectory(Long propertyId) {
        return "properties/" + propertyId;
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
}



