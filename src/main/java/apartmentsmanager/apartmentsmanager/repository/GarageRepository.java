package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.entity.Garage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
    
    List<Garage> findByBuilding(Building building);
    
    List<Garage> findByBuildingId(Long buildingId);
    
    Optional<Garage> findByBuildingAndGarageNumber(Building building, String garageNumber);
    
    boolean existsByBuildingIdAndGarageNumber(Long buildingId, String garageNumber);
}

