package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Building;

import java.util.List;
import java.util.Optional;

public interface BuildingService {
    
    List<Building> getAllBuildings();
    
    Optional<Building> getBuildingById(Long id);
    
    Optional<Building> getBuildingByName(String name);
    
    Building saveBuilding(Building building);
    
    void deleteBuilding(Long id);
    
    boolean buildingExists(String name);
    
    Building updateBuildingStatus(Long buildingId, String status);
    
    Building updateBuildingStage(Long buildingId, String stage);
}


