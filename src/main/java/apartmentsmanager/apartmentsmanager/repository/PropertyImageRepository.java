package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
    
    List<PropertyImage> findByPropertyIdOrderByDisplayOrderAsc(Long propertyId);
    
    List<PropertyImage> findByPropertyIdAndIsPrimaryTrue(Long propertyId);
}





