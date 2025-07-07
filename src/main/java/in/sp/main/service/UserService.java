package in.sp.main.service;

import in.sp.main.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User registerUser(User user);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findById(Long id);
    
    List<User> findAll();
    
    List<User> findByRole(User.UserRole role);
    
    User updateUser(User user);
    
    void deleteUser(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    User verifyUser(String token);
    
    String generateVerificationToken();
    
    void sendVerificationEmail(User user, String token);
    
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    void resetPassword(String email);
} 