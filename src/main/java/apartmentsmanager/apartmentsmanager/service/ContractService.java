package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Apartment;

public interface ContractService {
    
    /**
     * Generate contract text file for apartment
     */
    byte[] generateContract(Apartment apartment);
    
    /**
     * Get contract filename
     */
    String getContractFilename(Apartment apartment);
}




