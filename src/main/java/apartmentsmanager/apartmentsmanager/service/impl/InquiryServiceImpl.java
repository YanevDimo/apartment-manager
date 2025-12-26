package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Inquiry;
import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.repository.InquiryRepository;
import apartmentsmanager.apartmentsmanager.repository.UserRepository;
import apartmentsmanager.apartmentsmanager.service.InquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InquiryServiceImpl implements InquiryService {
    
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public InquiryServiceImpl(InquiryRepository inquiryRepository, UserRepository userRepository) {
        this.inquiryRepository = inquiryRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public Inquiry saveInquiry(Inquiry inquiry) {
        return inquiryRepository.save(inquiry);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Inquiry> getInquiryById(Long id) {
        return inquiryRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Inquiry> getInquiriesByUserId(Long userId) {
        return inquiryRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Inquiry> getInquiriesByPropertyId(Long propertyId) {
        return inquiryRepository.findByPropertyId(propertyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Inquiry> getInquiriesByAgentId(Long agentId) {
        return inquiryRepository.findByPropertyAgentId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Inquiry> getInquiriesByStatus(Inquiry.InquiryStatus status) {
        return inquiryRepository.findAll().stream()
                .filter(inquiry -> inquiry.getStatus() == status)
                .toList();
    }
    
    @Override
    public Inquiry respondToInquiry(Long inquiryId, String response, Long respondedByUserId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));
        
        User respondedBy = userRepository.findById(respondedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + respondedByUserId));
        
        inquiry.setAgentResponse(response);
        inquiry.setRespondedBy(respondedBy);
        inquiry.setRespondedAt(LocalDateTime.now());
        inquiry.setStatus(Inquiry.InquiryStatus.CONTACTED);
        
        return saveInquiry(inquiry);
    }
    
    @Override
    public void updateInquiryStatus(Long inquiryId, Inquiry.InquiryStatus status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));
        inquiry.setStatus(status);
        saveInquiry(inquiry);
    }
    
    @Override
    public void deleteInquiry(Long id) {
        inquiryRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countInquiriesByStatus(Inquiry.InquiryStatus status) {
        return inquiryRepository.countByStatus(status);
    }
}




