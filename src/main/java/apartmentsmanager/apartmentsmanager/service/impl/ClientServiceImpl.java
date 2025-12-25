package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Client;
import apartmentsmanager.apartmentsmanager.repository.ClientRepository;
import apartmentsmanager.apartmentsmanager.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    
    private final ClientRepository clientRepository;
    
    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAllByOrderByNameAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }
    
    @Override
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }
    
    @Override
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Client> searchClients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllClients();
        }
        return clientRepository.searchClients(searchTerm);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findByEgn(String egn) {
        return clientRepository.findByEgn(egn);
    }
}
