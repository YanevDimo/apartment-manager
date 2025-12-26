package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService {
    
    List<Client> getAllClients();
    
    Optional<Client> getClientById(Long id);
    
    Optional<Client> getClientByIdWithApartmentsAndPayments(Long id);
    
    Client saveClient(Client client);
    
    void deleteClient(Long id);
    
    List<Client> searchClients(String searchTerm);
    
    Optional<Client> findByEgn(String egn);
    
    Long getApartmentCountForClient(Long clientId);
}
