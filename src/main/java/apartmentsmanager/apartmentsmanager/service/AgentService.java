package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.Agent;
import apartmentsmanager.apartmentsmanager.entity.User;

import java.util.List;
import java.util.Optional;

public interface AgentService {
    
    Agent saveAgent(Agent agent);
    
    Optional<Agent> getAgentById(Long id);
    
    Optional<Agent> getAgentByUserId(Long userId);
    
    Optional<Agent> getAgentByLicenseNumber(String licenseNumber);
    
    List<Agent> getAllAgents();
    
    Agent createAgentFromUser(User user, String licenseNumber);
    
    Agent updateAgent(Agent agent);
    
    void deleteAgent(Long id);
    
    boolean licenseNumberExists(String licenseNumber);
    
    long countAgents();
}


