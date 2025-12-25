package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Agent;
import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.repository.AgentRepository;
import apartmentsmanager.apartmentsmanager.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AgentServiceImpl implements AgentService {
    
    private final AgentRepository agentRepository;
    
    @Autowired
    public AgentServiceImpl(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }
    
    @Override
    public Agent saveAgent(Agent agent) {
        return agentRepository.save(agent);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Agent> getAgentById(Long id) {
        return agentRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Agent> getAgentByUserId(Long userId) {
        return agentRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Agent> getAgentByLicenseNumber(String licenseNumber) {
        return agentRepository.findByLicenseNumber(licenseNumber);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }
    
    @Override
    public Agent createAgentFromUser(User user, String licenseNumber) {
        if (licenseNumberExists(licenseNumber)) {
            throw new IllegalArgumentException("License number already exists: " + licenseNumber);
        }
        
        Agent agent = new Agent();
        agent.setUser(user);
        agent.setLicenseNumber(licenseNumber);
        return saveAgent(agent);
    }
    
    @Override
    public Agent updateAgent(Agent agent) {
        Agent existingAgent = agentRepository.findById(agent.getId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agent.getId()));
        
        // Check license number uniqueness if changed
        if (!existingAgent.getLicenseNumber().equals(agent.getLicenseNumber()) && 
            licenseNumberExists(agent.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already exists: " + agent.getLicenseNumber());
        }
        
        return saveAgent(agent);
    }
    
    @Override
    public void deleteAgent(Long id) {
        agentRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean licenseNumberExists(String licenseNumber) {
        return agentRepository.existsByLicenseNumber(licenseNumber);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countAgents() {
        return agentRepository.count();
    }
}


