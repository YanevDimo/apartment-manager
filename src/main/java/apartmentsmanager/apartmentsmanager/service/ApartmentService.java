package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Apartment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ApartmentService {
    
    List<Apartment> getAllApartments();
    
    List<Apartment> getAllSoldApartments();
    
    Optional<Apartment> getApartmentById(Long id);
    
    Apartment saveApartment(Apartment apartment);
    
    void deleteApartment(Long id);
    
    boolean apartmentExists(String buildingName, String apartmentNumber, Long excludeId);
    
    List<Apartment> getApartmentsWithOverduePayments();
    
    BigDecimal getTotalRevenue();
    
    BigDecimal getTotalCollectedPayments();
    
    BigDecimal getTotalExpectedPayments();
    
    long getTotalApartmentsCount();
    
    Apartment updateApartmentStage(Long apartmentId, String stage);
    
    void updateAllApartmentsStage(String stage);
}

