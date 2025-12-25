package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Property;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PropertyService {
    
    Property saveProperty(Property property);
    
    Optional<Property> getPropertyById(Long id);
    
    List<Property> getAllProperties();
    
    List<Property> getPublishedProperties();
    
    List<Property> getFeaturedProperties();
    
    List<Property> getRecentProperties(int limit);
    
    List<Property> getPropertiesByAgent(Long agentId);
    
    List<Property> searchProperties(String city, Property.PropertyType type, 
                                     BigDecimal minPrice, BigDecimal maxPrice);
    
    List<String> getAllCities();
    
    void deleteProperty(Long id);
    
    long countPublishedProperties();
    
    long countPropertiesByAgent(Long agentId);
}

