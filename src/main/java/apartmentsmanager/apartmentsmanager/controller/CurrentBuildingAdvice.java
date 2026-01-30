package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.service.BuildingService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentBuildingAdvice {
    private final BuildingService buildingService;

    public CurrentBuildingAdvice(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @ModelAttribute
    public void addCurrentBuilding(Model model) {
        buildingService.getOrSetCurrentBuilding().ifPresent(building -> {
            model.addAttribute("currentBuildingId", building.getId());
            model.addAttribute("currentBuildingName", building.getName());
        });
    }
}
