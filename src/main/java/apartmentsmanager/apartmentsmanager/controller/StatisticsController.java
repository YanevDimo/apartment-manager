package apartmentsmanager.apartmentsmanager.controller;

import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.BuildingService;
import apartmentsmanager.apartmentsmanager.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;

    public StatisticsController(StatisticsService statisticsService, BuildingService buildingService, ApartmentService apartmentService) {
        this.statisticsService = statisticsService;
        this.buildingService = buildingService;
        this.apartmentService = apartmentService;
    }

    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Long buildingId = buildingService.getCurrentBuildingId().orElse(null);
        Map<String, Object> stats = new HashMap<>();

        if (buildingId == null) {
            stats.put("totalApartments", 0);
            stats.put("totalRevenue", 0);
            stats.put("totalCollected", 0);
            stats.put("totalExpected", 0);
            stats.put("collectionRate", 0);
            stats.put("remainingPayments", 0);
            stats.put("overdueCount", 0);
            stats.put("currentBuildingId", null);
            return ResponseEntity.ok(stats);
        }

        stats.putAll(statisticsService.getStatisticsForBuilding(buildingId));
        stats.put("overdueCount", apartmentService.getApartmentsWithOverduePaymentsByBuilding(buildingId).size());
        stats.put("currentBuildingId", buildingId);
        return ResponseEntity.ok(stats);
    }
}
