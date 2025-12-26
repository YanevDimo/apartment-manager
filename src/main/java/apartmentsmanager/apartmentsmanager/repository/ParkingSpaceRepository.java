package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    
    List<ParkingSpace> findByBuilding(Building building);
    
    List<ParkingSpace> findByBuildingId(Long buildingId);
    
    Optional<ParkingSpace> findByBuildingAndParkingNumber(Building building, String parkingNumber);
    
    boolean existsByBuildingIdAndParkingNumber(Long buildingId, String parkingNumber);
}


