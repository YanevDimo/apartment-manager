package apartmentsmanager.apartmentsmanager.repository;

import apartmentsmanager.apartmentsmanager.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    
    List<Inquiry> findByUserId(Long userId);
    
    List<Inquiry> findByPropertyId(Long propertyId);
    
    List<Inquiry> findByPropertyAgentId(Long agentId);
    
    long countByStatus(Inquiry.InquiryStatus status);
}



