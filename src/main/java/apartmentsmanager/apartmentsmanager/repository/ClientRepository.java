package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    // Find all ordered by name
    List<Client> findAllByOrderByNameAsc();
    
    // Search clients by name, phone, or email
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Client> searchClients(@Param("search") String search);
    
    // Find by EGN/EIK
    Optional<Client> findByEgn(String egn);
    
    // Find client with apartments eagerly loaded (payments will be initialized separately to avoid MultipleBagFetchException)
    @EntityGraph(attributePaths = {"apartments"})
    @Query("SELECT c FROM Client c WHERE c.id = :id")
    Optional<Client> findByIdWithApartmentsAndPayments(@Param("id") Long id);
    
    // Get apartment count for a client
    @Query("SELECT COUNT(a) FROM Apartment a WHERE a.client.id = :clientId")
    Long countApartmentsByClientId(@Param("clientId") Long clientId);
}
