package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Inquiry;

import java.util.List;
import java.util.Optional;

public interface InquiryService {
    
    Inquiry saveInquiry(Inquiry inquiry);
    
    Optional<Inquiry> getInquiryById(Long id);
    
    List<Inquiry> getAllInquiries();
    
    List<Inquiry> getInquiriesByUserId(Long userId);
    
    List<Inquiry> getInquiriesByPropertyId(Long propertyId);
    
    List<Inquiry> getInquiriesByAgentId(Long agentId);
    
    List<Inquiry> getInquiriesByStatus(Inquiry.InquiryStatus status);
    
    Inquiry respondToInquiry(Long inquiryId, String response, Long respondedByUserId);
    
    void updateInquiryStatus(Long inquiryId, Inquiry.InquiryStatus status);
    
    void deleteInquiry(Long id);
    
    long countInquiriesByStatus(Inquiry.InquiryStatus status);
}


