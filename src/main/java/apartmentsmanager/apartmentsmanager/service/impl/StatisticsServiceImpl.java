package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import apartmentsmanager.apartmentsmanager.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
    
    private final ApartmentService apartmentService;
    
    @Autowired
    public StatisticsServiceImpl(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalApartments() {
        return apartmentService.getTotalApartmentsCount();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return apartmentService.getTotalRevenue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCollectedPayments() {
        return apartmentService.getTotalCollectedPayments();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedPayments() {
        return apartmentService.getTotalExpectedPayments();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCollectionRate() {
        BigDecimal totalRevenue = getTotalRevenue();
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal collected = getTotalCollectedPayments();
        return collected.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApartments", getTotalApartments());
        stats.put("totalRevenue", getTotalRevenue());
        stats.put("totalCollected", getTotalCollectedPayments());
        stats.put("totalExpected", getTotalExpectedPayments());
        stats.put("collectionRate", getCollectionRate());
        stats.put("remainingPayments", getTotalRevenue().subtract(getTotalCollectedPayments()));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatisticsForBuilding(Long buildingId) {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalRevenue = apartmentService.getTotalRevenueByBuilding(buildingId);
        BigDecimal totalCollected = apartmentService.getTotalCollectedPaymentsByBuilding(buildingId);
        BigDecimal totalExpected = apartmentService.getTotalExpectedPaymentsByBuilding(buildingId);
        long totalApartments = apartmentService.getTotalApartmentsCountByBuilding(buildingId);

        BigDecimal collectionRate = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = totalCollected.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        stats.put("totalApartments", totalApartments);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCollected", totalCollected);
        stats.put("totalExpected", totalExpected);
        stats.put("collectionRate", collectionRate);
        stats.put("remainingPayments", totalExpected.subtract(totalCollected));
        stats.put("stageBreakdown", buildStageBreakdown(buildingId));
        stats.put("paymentBreakdown", buildPaymentBreakdown(buildingId));
        return stats;
    }

    private Map<String, Long> buildStageBreakdown(Long buildingId) {
        Map<String, Long> breakdown = new HashMap<>();
        breakdown.put("При предварителен договор", 0L);
        breakdown.put("Акт 14", 0L);
        breakdown.put("Акт 15", 0L);
        breakdown.put("Акт 16", 0L);

        apartmentService.getAllSoldApartmentsByBuilding(buildingId).forEach(apt -> {
            String stage = apt.getStage();
            if (stage == null || stage.isBlank()) {
                stage = "При предварителен договор";
            }
            if ("Предварителен договор".equals(stage)) {
                stage = "При предварителен договор";
            }
            breakdown.put(stage, breakdown.getOrDefault(stage, 0L) + 1L);
        });

        return breakdown;
    }

    private Map<String, Object> buildPaymentBreakdown(Long buildingId) {
        Map<String, Object> breakdown = new HashMap<>();
        Map<String, BigDecimal> expected = new HashMap<>();
        Map<String, BigDecimal> collected = new HashMap<>();

        expected.put("prelim", BigDecimal.ZERO);
        expected.put("akt14", BigDecimal.ZERO);
        expected.put("akt15", BigDecimal.ZERO);
        expected.put("akt16", BigDecimal.ZERO);

        collected.put("prelim", BigDecimal.ZERO);
        collected.put("akt14", BigDecimal.ZERO);
        collected.put("akt15", BigDecimal.ZERO);
        collected.put("akt16", BigDecimal.ZERO);

        apartmentService.getAllSoldApartmentsByBuilding(buildingId).forEach(apt -> {
            if (apt.getPaymentPlan() != null) {
                var plan = apt.getPaymentPlan();
                expected.put("prelim", expected.get("prelim").add(plan.getPreliminaryContractAmount() != null ? plan.getPreliminaryContractAmount() : BigDecimal.ZERO));
                expected.put("akt14", expected.get("akt14").add(plan.getAkt14Amount() != null ? plan.getAkt14Amount() : BigDecimal.ZERO));
                expected.put("akt15", expected.get("akt15").add(plan.getAkt15Amount() != null ? plan.getAkt15Amount() : BigDecimal.ZERO));
                expected.put("akt16", expected.get("akt16").add(plan.getAkt16Amount() != null ? plan.getAkt16Amount() : BigDecimal.ZERO));
            }

            if (apt.getPayments() != null) {
                apt.getPayments().forEach(payment -> {
                    String stage = payment.getPaymentStage();
                    BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
                    if (stage == null) {
                        return;
                    }
                    if (stage.contains("предварителен") || stage.toLowerCase().contains("prelim")) {
                        collected.put("prelim", collected.get("prelim").add(amount));
                    } else if (stage.contains("Акт 14") || stage.contains("акт 14")) {
                        collected.put("akt14", collected.get("akt14").add(amount));
                    } else if (stage.contains("Акт 15") || stage.contains("акт 15")) {
                        collected.put("akt15", collected.get("akt15").add(amount));
                    } else if (stage.contains("Акт 16") || stage.contains("акт 16")) {
                        collected.put("akt16", collected.get("akt16").add(amount));
                    }
                });
            }
        });

        breakdown.put("expected", expected);
        breakdown.put("collected", collected);
        return breakdown;
    }
}






