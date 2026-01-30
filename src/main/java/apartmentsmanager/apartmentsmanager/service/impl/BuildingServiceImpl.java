package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.dto.BuildingDetailDto;
import apartmentsmanager.apartmentsmanager.dto.BuildingRowDto;
import apartmentsmanager.apartmentsmanager.entity.Building;
import apartmentsmanager.apartmentsmanager.repository.BuildingRepository;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Transactional
public class BuildingServiceImpl implements BuildingService {
    
    private final BuildingRepository buildingRepository;
    private final AtomicReference<Long> currentBuildingId = new AtomicReference<>(null);
    
    @Autowired
    public BuildingServiceImpl(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBuildingsPageData() {
        Map<String, Object> data = new HashMap<>();
        try {
            List<Building> buildings = buildingRepository.findAll();
            List<BuildingRowDto> buildingRows = toRowDtos(buildings);
            data.put("buildings", buildingRows);
            data.put("buildingsCount", buildingRows.size());
            data.put("currentBuilding", null);
            data.put("apartmentsCount", 0);
            data.put("garagesCount", 0);
            data.put("basementsCount", 0);
            data.put("parkingSpacesCount", 0);
            data.put("commercialSpacesCount", 0);
            if (buildings != null && !buildings.isEmpty()) {
                Building currentEntity = null;
                Long selectedId = currentBuildingId.get();
                if (selectedId != null) {
                    for (Building b : buildings) {
                        if (selectedId.equals(b.getId())) {
                            currentEntity = b;
                            break;
                        }
                    }
                    if (currentEntity == null) {
                        currentBuildingId.set(null);
                    }
                }
                if (currentEntity == null) {
                    currentEntity = buildings.stream()
                        .filter(b -> b.getStatus() != null && b.getStatus().equals("активна"))
                        .findFirst()
                        .orElse(buildings.get(0));
                }
                data.put("currentBuilding", toDetailDto(currentEntity));
                data.put("apartmentsCount", safeSize(currentEntity.getApartments()));
                data.put("garagesCount", safeSize(currentEntity.getGarages()));
                data.put("basementsCount", safeSize(currentEntity.getBasements()));
                data.put("parkingSpacesCount", safeSize(currentEntity.getParkingSpaces()));
                data.put("commercialSpacesCount", safeSize(currentEntity.getCommercialSpaces()));
            }
        } catch (Exception e) {
            data.put("buildings", List.of());
            data.put("buildingsCount", 0);
            data.put("currentBuilding", null);
            data.put("apartmentsCount", 0);
            data.put("garagesCount", 0);
            data.put("basementsCount", 0);
            data.put("parkingSpacesCount", 0);
            data.put("commercialSpacesCount", 0);
            data.put("error", e.getMessage());
        }
        return data;
    }

    /** Конвертира entity -> DTO само с прости полета (без lazy колекции). View получава само DTO. */
    private static List<BuildingRowDto> toRowDtos(List<Building> buildings) {
        if (buildings == null) return List.of();
        List<BuildingRowDto> list = new ArrayList<>(buildings.size());
        for (Building b : buildings) {
            int apartmentsCount = safeSize(b.getApartments());
            int garagesCount = safeSize(b.getGarages());
            int basementsCount = safeSize(b.getBasements());
            int parkingSpacesCount = safeSize(b.getParkingSpaces());
            int commercialSpacesCount = safeSize(b.getCommercialSpaces());
            int totalCount = apartmentsCount + garagesCount + basementsCount + parkingSpacesCount + commercialSpacesCount;
            list.add(new BuildingRowDto(
                b.getId(),
                b.getName(),
                b.getAddress(),
                b.getStatus(),
                b.getStage(),
                b.getCreatedAt(),
                apartmentsCount,
                garagesCount,
                basementsCount,
                parkingSpacesCount,
                commercialSpacesCount,
                totalCount
            ));
        }
        return list;
    }

    private static BuildingDetailDto toDetailDto(Building b) {
        if (b == null) return null;
        return new BuildingDetailDto(
            b.getId(),
            b.getName(),
            b.getAddress(),
            b.getStatus(),
            b.getStage(),
            b.getNotes(),
            b.getCreatedAt(),
            b.getUpdatedAt()
        );
    }

    private static int safeSize(java.util.Collection<?> c) {
        return c != null ? c.size() : 0;
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
        if (id != null && id.equals(currentBuildingId.get())) {
            currentBuildingId.set(null);
        }
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

    @Override
    public void setCurrentBuildingId(Long buildingId) {
        currentBuildingId.set(buildingId);
    }

    @Override
    public Optional<Long> getCurrentBuildingId() {
        return Optional.ofNullable(currentBuildingId.get());
    }
}



