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
        return stats;
    }
}






