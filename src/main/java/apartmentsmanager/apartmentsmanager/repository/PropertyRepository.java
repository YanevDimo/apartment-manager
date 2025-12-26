package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    List<Property> findByPublishedTrue();
    
    List<Property> findByPublishedTrueAndFeaturedTrue();
    
    List<Property> findByAgentId(Long agentId);
    
    List<Property> findByCity(String city);
    
    List<Property> findByType(Property.PropertyType type);
    
    @Query("SELECT p FROM Property p WHERE p.published = true AND " +
           "(:city IS NULL OR p.city = :city) AND " +
           "(:type IS NULL OR p.type = :type) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Property> searchProperties(@Param("city") String city,
                                     @Param("type") Property.PropertyType type,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT DISTINCT p.city FROM Property p WHERE p.published = true ORDER BY p.city")
    List<String> findAllCities();
    
    long countByPublishedTrue();
    
    long countByAgentId(Long agentId);
}




