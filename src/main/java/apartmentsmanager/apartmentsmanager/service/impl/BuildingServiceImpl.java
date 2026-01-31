package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.dto.BuildingDetailDto;
import apartmentsmanager.apartmentsmanager.dto.BuildingRowDto;
import apartmentsmanager.apartmentsmanager.entity.Apartment;
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
                CountSummary summary = buildCounts(currentEntity);
                data.put("apartmentsCount", summary.apartments);
                data.put("garagesCount", summary.garages);
                data.put("basementsCount", summary.basements);
                data.put("parkingSpacesCount", summary.parkingSpaces);
                data.put("commercialSpacesCount", summary.commercialSpaces);
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
            CountSummary summary = buildCounts(b);
            list.add(new BuildingRowDto(
                b.getId(),
                b.getName(),
                b.getAddress(),
                b.getStatus(),
                b.getStage(),
                b.getCreatedAt(),
                summary.apartments,
                summary.garages,
                summary.basements,
                summary.parkingSpaces,
                summary.commercialSpaces,
                summary.total
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

    private static CountSummary buildCounts(Building building) {
        CountSummary summary = new CountSummary();
        if (building == null) return summary;

        List<Apartment> apartments = building.getApartments();
        CountSummary classified = classifyFromApartments(apartments);

        boolean hasSpecialTypes = classified.garages > 0 ||
            classified.basements > 0 ||
            classified.parkingSpaces > 0 ||
            classified.commercialSpaces > 0;

        if (hasSpecialTypes) {
            summary = classified;
        } else {
            summary.apartments = safeSize(building.getApartments());
            summary.garages = safeSize(building.getGarages());
            summary.basements = safeSize(building.getBasements());
            summary.parkingSpaces = safeSize(building.getParkingSpaces());
            summary.commercialSpaces = safeSize(building.getCommercialSpaces());
            summary.total = summary.apartments + summary.garages + summary.basements + summary.parkingSpaces + summary.commercialSpaces;
        }

        return summary;
    }

    private static CountSummary classifyFromApartments(List<Apartment> apartments) {
        CountSummary summary = new CountSummary();
        if (apartments == null) return summary;

        for (Apartment apartment : apartments) {
            String number = apartment != null ? apartment.getApartmentNumber() : null;
            String value = number != null ? number.trim().toUpperCase() : "";

            if (value.contains("ГАРАЖ")) {
                summary.garages++;
            } else if (value.contains("ПАРКО")) {
                summary.parkingSpaces++;
            } else if (value.contains("МАЗЕ")) {
                summary.basements++;
            } else if (value.contains("ТЪРГ") || value.contains("МАГАЗ")) {
                summary.commercialSpaces++;
            } else {
                summary.apartments++;
            }
        }

        summary.total = summary.apartments + summary.garages + summary.basements + summary.parkingSpaces + summary.commercialSpaces;
        return summary;
    }

    private static class CountSummary {
        int apartments;
        int garages;
        int basements;
        int parkingSpaces;
        int commercialSpaces;
        int total;
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
    @Transactional
    public Building updateBuildingStage(Long buildingId, String stage) {
        Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new RuntimeException("Сграда с ID " + buildingId + " не е намерена"));
        
        // Update building stage
        building.setStage(stage);
        
        // Map building stage to apartment stage
        String apartmentStage = mapBuildingStageToApartmentStage(stage);
        
        // Update all apartments/objects in this building to the new stage
        if (building.getApartments() != null && !building.getApartments().isEmpty()) {
            building.getApartments().forEach(apt -> apt.setStage(apartmentStage));
        }
        
        return buildingRepository.save(building);
    }
    
    /**
     * Map building stage to apartment stage
     * Building stages: "Открита строителна площадка", "Акт 14", "Акт 15", "Акт 16"
     * Apartment stages: "Предварителен договор", "Акт 14", "Акт 15", "Акт 16"
     */
    private String mapBuildingStageToApartmentStage(String buildingStage) {
        if (buildingStage == null) {
            return "Предварителен договор";
        }
        
        switch (buildingStage) {
            case "Открита строителна площадка":
                return "Предварителен договор";
            case "Акт 14":
                return "Акт 14";
            case "Акт 15":
                return "Акт 15";
            case "Акт 16":
                return "Акт 16";
            default:
                return "Предварителен договор";
        }
    }

    @Override
    public void setCurrentBuildingId(Long buildingId) {
        currentBuildingId.set(buildingId);
    }

    @Override
    public Optional<Long> getCurrentBuildingId() {
        return Optional.ofNullable(currentBuildingId.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Building> getOrSetCurrentBuilding() {
        Long selectedId = currentBuildingId.get();
        if (selectedId != null) {
            return buildingRepository.findById(selectedId);
        }

        List<Building> buildings = buildingRepository.findAll();
        if (buildings == null || buildings.isEmpty()) {
            return Optional.empty();
        }

        Building chosen = buildings.stream()
            .filter(b -> b.getStatus() != null && b.getStatus().equals("активна"))
            .findFirst()
            .orElse(buildings.get(0));

        currentBuildingId.set(chosen.getId());
        return Optional.of(chosen);
    }
}



