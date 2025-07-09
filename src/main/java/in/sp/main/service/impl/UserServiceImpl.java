package in.sp.main.service.impl;

import in.sp.main.entity.User;
import in.sp.main.repository.UserRepository;
import in.sp.main.service.OTPService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    
    @Override
    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Check if phone already exists
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }
        
        // Set user as unverified initially
        user.setVerified(false);
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Send OTP for email verification
        String otp = otpService.generateOTP();
        otpService.sendOTPEmail(savedUser, otp);
        
        return savedUser;
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    @Override
    public List<User> findByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    @Override
    public User verifyUser(String token) {
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(user -> token.equals(user.getVerificationToken()))
                .findFirst();
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(true);
            user.setVerificationToken(null);
            return userRepository.save(user);
        }
        
        throw new RuntimeException("Invalid verification token");
    }
    
    @Override
    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public void sendVerificationEmail(User user, String token) {
        // Implementation for email sending logic
        // In a real application, this would use JavaMailSender or similar
        String verificationLink = "http://localhost:8081/verify?token=" + token;
        
        // For development, just print the verification link
        System.out.println("=== EMAIL VERIFICATION ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Verify your Spincery account");
        System.out.println("Body: Please click the following link to verify your account: " + verificationLink);
        System.out.println("==========================");
        
        // TODO: In production, implement actual email sending using JavaMailSender
        // JavaMailSender mailSender;
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(user.getEmail());
        // message.setSubject("Verify your Spincery account");
        // message.setText("Please click the following link to verify your account: " + verificationLink);
        // mailSender.send(message);
    }
    
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void resetPassword(String email) {
        // Implementation for password reset logic
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            user.setVerificationToken(resetToken);
            userRepository.save(user);
            
            // Send password reset email
            String resetLink = "http://localhost:8081/reset-password?token=" + resetToken;
            
            System.out.println("=== PASSWORD RESET ===");
            System.out.println("To: " + user.getEmail());
            System.out.println("Subject: Reset your Spincery password");
            System.out.println("Body: Please click the following link to reset your password: " + resetLink);
            System.out.println("======================");
            
            // TODO: In production, implement actual email sending using JavaMailSender
            // JavaMailSender mailSender;
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setTo(user.getEmail());
            // message.setSubject("Reset your Spincery password");
            // message.setText("Please click the following link to reset your password: " + resetLink);
            // mailSender.send(message);
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }
    
    @Override
    public void sendOTPToAllUnverifiedUsers() {
        otpService.sendOTPToAllUnverifiedUsers();
    }
    
    @Override
    public boolean validateOTP(String email, String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return otpService.validateOTP(userOpt.get(), otp);
        }
        return false;
    }
    
    @Override
    public void resendOTP(String email) {
        otpService.resendOTP(email);
    }
} 