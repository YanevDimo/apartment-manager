package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    
    // Find by building and apartment number (for duplicate check)
    Optional<Apartment> findByBuildingAndApartmentNumber(Building building, String apartmentNumber);
    
    // Find by building name and apartment number (for backward compatibility)
    Optional<Apartment> findByBuildingNameAndApartmentNumber(String buildingName, String apartmentNumber);
    
    // Find by building
    List<Apartment> findByBuilding(Building building);
    
    // Find by building ID
    List<Apartment> findByBuildingId(Long buildingId);

    // Find sold apartments by building ID
    List<Apartment> findByIsSoldTrueAndBuildingId(Long buildingId);
    
    // Find all sold apartments
    List<Apartment> findByIsSoldTrue();
    
    // Find apartments with overdue payments
    @Query("SELECT DISTINCT a FROM Apartment a " +
           "JOIN a.paymentPlan pp " +
           "WHERE a.isSold = true " +
           "AND (pp.preliminaryContractDate < CURRENT_DATE OR " +
           "     pp.akt14Date < CURRENT_DATE OR " +
           "     pp.akt15Date < CURRENT_DATE OR " +
           "     pp.akt16Date < CURRENT_DATE)")
    List<Apartment> findApartmentsWithOverduePayments();

    // Find apartments with overdue payments by building
    @Query("SELECT DISTINCT a FROM Apartment a " +
           "JOIN a.paymentPlan pp " +
           "WHERE a.isSold = true AND a.building.id = :buildingId " +
           "AND (pp.preliminaryContractDate < CURRENT_DATE OR " +
           "     pp.akt14Date < CURRENT_DATE OR " +
           "     pp.akt15Date < CURRENT_DATE OR " +
           "     pp.akt16Date < CURRENT_DATE)")
    List<Apartment> findApartmentsWithOverduePaymentsByBuilding(Long buildingId);
    
    // Calculate total revenue
    @Query("SELECT COALESCE(SUM(a.totalPrice), 0) FROM Apartment a WHERE a.isSold = true")
    BigDecimal calculateTotalRevenue();

    // Calculate total revenue by building
    @Query("SELECT COALESCE(SUM(a.totalPrice), 0) FROM Apartment a WHERE a.isSold = true AND a.building.id = :buildingId")
    BigDecimal calculateTotalRevenueByBuilding(Long buildingId);
    
    // Calculate total collected payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "JOIN p.apartment a WHERE a.isSold = true")
    BigDecimal calculateTotalCollectedPayments();

    // Calculate total collected payments by building
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "JOIN p.apartment a WHERE a.isSold = true AND a.building.id = :buildingId")
    BigDecimal calculateTotalCollectedPaymentsByBuilding(Long buildingId);

    // Count sold apartments by building
    @Query("SELECT COUNT(a) FROM Apartment a WHERE a.isSold = true AND a.building.id = :buildingId")
    long countSoldApartmentsByBuilding(Long buildingId);
    
    // Count apartments by stage
    @Query("SELECT a.stage, COUNT(a) FROM Apartment a WHERE a.isSold = true GROUP BY a.stage")
    List<Object[]> countApartmentsByStage();
    
    // Find all with payments eagerly loaded
    @Query("SELECT DISTINCT a FROM Apartment a LEFT JOIN FETCH a.payments WHERE a.isSold = true")
    List<Apartment> findAllSoldApartmentsWithPayments();

    // Find all sold apartments with payments by building
    @Query("SELECT DISTINCT a FROM Apartment a LEFT JOIN FETCH a.payments WHERE a.isSold = true AND a.building.id = :buildingId")
    List<Apartment> findAllSoldApartmentsWithPaymentsByBuilding(Long buildingId);
}

