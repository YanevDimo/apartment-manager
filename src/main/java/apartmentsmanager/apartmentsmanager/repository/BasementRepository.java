package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Basement;
import apartmentsmanager.apartmentsmanager.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasementRepository extends JpaRepository<Basement, Long> {
    
    List<Basement> findByBuilding(Building building);
    
    List<Basement> findByBuildingId(Long buildingId);
    
    Optional<Basement> findByBuildingAndBasementNumber(Building building, String basementNumber);
    
    boolean existsByBuildingIdAndBasementNumber(Long buildingId, String basementNumber);
}


