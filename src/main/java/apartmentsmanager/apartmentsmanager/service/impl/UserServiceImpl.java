package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.User;
import apartmentsmanager.apartmentsmanager.repository.UserRepository;
import apartmentsmanager.apartmentsmanager.service.AgentService;
import apartmentsmanager.apartmentsmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgentService agentService;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AgentService agentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.agentService = agentService;
    }
    
    @Override
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            // Password is not encrypted, encrypt it
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }
    
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public User registerUser(User user) {
        if (usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        user.setRole(User.Role.USER); // Default role
        user.setActive(true);
        User savedUser = saveUser(user);
        
        // If role is AGENT, create Agent record automatically
        if (savedUser.getRole() == User.Role.AGENT) {
            // Create agent with a default license number (can be updated later)
            String defaultLicense = "AGENT-" + savedUser.getId();
            agentService.createAgentFromUser(savedUser, defaultLicense);
        }
        
        return savedUser;
    }
    
    @Override
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
        
        // Check username uniqueness if changed
        if (!existingUser.getUsername().equals(user.getUsername()) && usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Check email uniqueness if changed
        if (!existingUser.getEmail().equals(user.getEmail()) && emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Preserve password if not changed
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }
        
        return saveUser(user);
    }
    
    @Override
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setActive(true);
        userRepository.save(user);
    }
    
    @Override
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
    }
    
    @Override
    public void changeUserRole(Long id, User.Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        // If changing to AGENT role and agent doesn't exist, create one
        if (newRole == User.Role.AGENT && !agentService.getAgentByUserId(id).isPresent()) {
            String defaultLicense = "AGENT-" + id;
            agentService.createAgentFromUser(user, defaultLicense);
        }
        
        user.setRole(newRole);
        userRepository.save(user);
    }
}

