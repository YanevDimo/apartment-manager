package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.repository.BuildingRepository;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BuildingServiceImpl implements BuildingService {
    
    private final BuildingRepository buildingRepository;
    
    @Autowired
    public BuildingServiceImpl(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }
    
    @Override
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }
    
    @Override
    public Optional<Building> getBuildingById(Long id) {
        return buildingRepository.findById(id);
    }
    
    @Override
    public Optional<Building> getBuildingByName(String name) {
        return buildingRepository.findByName(name);
    }
    
    @Override
    public Building saveBuilding(Building building) {
        return buildingRepository.save(building);
    }
    
    @Override
    public void deleteBuilding(Long id) {
        buildingRepository.deleteById(id);
    }
    
    @Override
    public boolean buildingExists(String name) {
        return buildingRepository.existsByName(name);
    }
    
    @Override
    public Building updateBuildingStatus(Long buildingId, String status) {
        Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new RuntimeException("Сграда с ID " + buildingId + " не е намерена"));
        building.setStatus(status);
        return buildingRepository.save(building);
    }
    
    @Override
    public Building updateBuildingStage(Long buildingId, String stage) {
        Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new RuntimeException("Сграда с ID " + buildingId + " не е намерена"));
        building.setStage(stage);
        return buildingRepository.save(building);
    }
}



