package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Apartment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ApartmentService {
    
    List<Apartment> getAllApartments();
    
    List<Apartment> getAllSoldApartments();

    List<Apartment> getAllSoldApartmentsByBuilding(Long buildingId);
    
    Optional<Apartment> getApartmentById(Long id);
    
    Apartment saveApartment(Apartment apartment);
    
    void deleteApartment(Long id);
    
    boolean apartmentExists(String buildingName, String apartmentNumber, Long excludeId);
    
    List<Apartment> getApartmentsWithOverduePayments();

    List<Apartment> getApartmentsWithOverduePaymentsByBuilding(Long buildingId);
    
    BigDecimal getTotalRevenue();

    BigDecimal getTotalRevenueByBuilding(Long buildingId);
    
    BigDecimal getTotalCollectedPayments();

    BigDecimal getTotalCollectedPaymentsByBuilding(Long buildingId);
    
    BigDecimal getTotalExpectedPayments();

    BigDecimal getTotalExpectedPaymentsByBuilding(Long buildingId);
    
    long getTotalApartmentsCount();

    long getTotalApartmentsCountByBuilding(Long buildingId);
    
    Apartment updateApartmentStage(Long apartmentId, String stage);
    
    void updateAllApartmentsStage(String stage);

    void updateAllApartmentsStageByBuilding(Long buildingId, String stage);
}

