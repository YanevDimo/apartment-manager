package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.repository.ApartmentRepository;
import apartmentsmanager.apartmentsmanager.service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApartmentServiceImpl implements ApartmentService {
    
    private final ApartmentRepository apartmentRepository;
    
    @Autowired
    public ApartmentServiceImpl(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Apartment> getAllApartments() {
        return apartmentRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Apartment> getAllSoldApartments() {
        return apartmentRepository.findAllSoldApartmentsWithPayments();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Apartment> getApartmentById(Long id) {
        return apartmentRepository.findById(id);
    }
    
    @Override
    public Apartment saveApartment(Apartment apartment) {
        return apartmentRepository.save(apartment);
    }
    
    @Override
    public void deleteApartment(Long id) {
        apartmentRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean apartmentExists(String buildingName, String apartmentNumber, Long excludeId) {
        Optional<Apartment> existing = apartmentRepository.findByBuildingNameAndApartmentNumber(
            buildingName, apartmentNumber);
        
        if (existing.isPresent()) {
            if (excludeId != null && existing.get().getId().equals(excludeId)) {
                return false; // Same apartment, not a duplicate
            }
            return true; // Duplicate found
        }
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Apartment> getApartmentsWithOverduePayments() {
        return apartmentRepository.findApartmentsWithOverduePayments();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal total = apartmentRepository.calculateTotalRevenue();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCollectedPayments() {
        BigDecimal total = apartmentRepository.calculateTotalCollectedPayments();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedPayments() {
        return getTotalRevenue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalApartmentsCount() {
        return apartmentRepository.findByIsSoldTrue().size();
    }
    
    @Override
    public Apartment updateApartmentStage(Long apartmentId, String stage) {
        Optional<Apartment> apartmentOpt = apartmentRepository.findById(apartmentId);
        if (apartmentOpt.isPresent()) {
            Apartment apartment = apartmentOpt.get();
            apartment.setStage(stage);
            return apartmentRepository.save(apartment);
        }
        throw new RuntimeException("Apartment not found with id: " + apartmentId);
    }
    
    @Override
    public void updateAllApartmentsStage(String stage) {
        List<Apartment> apartments = apartmentRepository.findByIsSoldTrue();
        for (Apartment apartment : apartments) {
            apartment.setStage(stage);
        }
        apartmentRepository.saveAll(apartments);
    }
}

