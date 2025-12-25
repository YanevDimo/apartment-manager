package apartmentsmanager.apartmentsmanager.service;

import java.math.BigDecimal;
import java.util.Map;

public interface StatisticsService {
    
    /**
     * Get total number of apartments
     */
    long getTotalApartments();
    
    /**
     * Get total revenue (sum of all apartment prices)
     */
    BigDecimal getTotalRevenue();
    
    /**
     * Get total collected payments
     */
    BigDecimal getTotalCollectedPayments();
    
    /**
     * Get total expected payments
     */
    BigDecimal getTotalExpectedPayments();
    
    /**
     * Get collection rate percentage
     */
    BigDecimal getCollectionRate();
    
    /**
     * Get statistics map
     */
    Map<String, Object> getStatistics();
}

