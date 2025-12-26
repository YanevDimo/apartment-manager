package apartmentsmanager.apartmentsmanager.service;

import apartmentsmanager.apartmentsmanager.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User saveUser(User user);
    
    Optional<User> getUserById(Long id);
    
    Optional<User> getUserByUsername(String username);
    
    Optional<User> getUserByEmail(String email);
    
    List<User> getAllUsers();
    
    List<User> getUsersByRole(User.Role role);
    
    void deleteUser(Long id);
    
    boolean usernameExists(String username);
    
    boolean emailExists(String email);
    
    User registerUser(User user);
    
    User updateUser(User user);
    
    void activateUser(Long id);
    
    void deactivateUser(Long id);
    
    void changeUserRole(Long id, User.Role newRole);
}



