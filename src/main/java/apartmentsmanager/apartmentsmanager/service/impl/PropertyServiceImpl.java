package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Property;
import apartmentsmanager.apartmentsmanager.repository.PropertyRepository;
import apartmentsmanager.apartmentsmanager.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyServiceImpl implements PropertyService {
    
    private final PropertyRepository propertyRepository;
    
    @Autowired
    public PropertyServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }
    
    @Override
    public Property saveProperty(Property property) {
        return propertyRepository.save(property);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Property> getPropertyById(Long id) {
        return propertyRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Property> getPublishedProperties() {
        return propertyRepository.findByPublishedTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Property> getFeaturedProperties() {
        return propertyRepository.findByPublishedTrueAndFeaturedTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Property> getRecentProperties(int limit) {
        return propertyRepository.findByPublishedTrue().stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(limit)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Property> getPropertiesByAgent(Long agentId) {
        return propertyRepository.findByAgentId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Property> searchProperties(String city, Property.PropertyType type, 
                                           BigDecimal minPrice, BigDecimal maxPrice) {
        return propertyRepository.searchProperties(city, type, minPrice, maxPrice);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        return propertyRepository.findAllCities();
    }
    
    @Override
    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countPublishedProperties() {
        return propertyRepository.countByPublishedTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countPropertiesByAgent(Long agentId) {
        return propertyRepository.countByAgentId(agentId);
    }
}


