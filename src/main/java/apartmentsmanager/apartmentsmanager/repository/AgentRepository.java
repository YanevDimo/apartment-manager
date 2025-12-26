package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    Optional<Agent> findByLicenseNumber(String licenseNumber);
    
    Optional<Agent> findByUserId(Long userId);
    
    boolean existsByLicenseNumber(String licenseNumber);
}





