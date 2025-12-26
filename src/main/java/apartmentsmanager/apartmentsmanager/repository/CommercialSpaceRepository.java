package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.entity.CommercialSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommercialSpaceRepository extends JpaRepository<CommercialSpace, Long> {
    
    List<CommercialSpace> findByBuilding(Building building);
    
    List<CommercialSpace> findByBuildingId(Long buildingId);
    
    Optional<CommercialSpace> findByBuildingAndCommercialNumber(Building building, String commercialNumber);
    
    boolean existsByBuildingIdAndCommercialNumber(Long buildingId, String commercialNumber);
}


