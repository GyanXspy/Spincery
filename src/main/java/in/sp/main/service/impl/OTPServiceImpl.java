package in.sp.main.service.impl;

import in.sp.main.entity.User;
import in.sp.main.repository.UserRepository;
import in.sp.main.service.OTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPServiceImpl implements OTPService {
    
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    @Override
    public String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    @Override
    public void sendOTPEmail(User user, String otp) {
        try {
            // Set OTP and expiry time
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            userRepository.save(user);
            
            log.info("OTP stored for user: {} - OTP: {}, Expiry: {}", user.getEmail(), otp, user.getOtpExpiry());
            
            // Create email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Spincery - Email Verification OTP");
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Thank you for registering with Spincery!\n\n" +
                "Your email verification OTP is: %s\n\n" +
                "This OTP will expire in %d minutes.\n\n" +
                "If you didn't request this verification, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Spincery Team",
                user.getName(), otp, OTP_EXPIRY_MINUTES
            );
            
            message.setText(emailBody);
            
            // Send email
            mailSender.send(message);
            
            log.info("OTP email sent successfully to: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", user.getEmail(), e);
            // Don't throw exception, just log it - OTP is still stored in database
            log.warn("Email sending failed, but OTP is stored. User can still verify with OTP: {}", otp);
        }
    }
    
    @Override
    public boolean validateOTP(User user, String otp) {
        log.info("Validating OTP for user: {}", user.getEmail());
        log.info("Stored OTP: '{}'", user.getOtp());
        log.info("Provided OTP: '{}'", otp);
        log.info("OTP Expiry: {}", user.getOtpExpiry());
        log.info("Current time: {}", LocalDateTime.now());
        
        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            log.warn("OTP or expiry is null for user: {}", user.getEmail());
            return false;
        }
        
        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            log.warn("OTP expired for user: {}", user.getEmail());
            return false;
        }
        
        // Check if OTP matches (trim to handle whitespace issues)
        boolean isValid = user.getOtp().trim().equals(otp.trim());
        
        log.info("OTP match result: {}", isValid);
        
        if (isValid) {
            // Clear OTP after successful validation
            user.setOtp(null);
            user.setOtpExpiry(null);
            user.setVerified(true);
            userRepository.save(user);
            log.info("OTP validated successfully for user: {}", user.getEmail());
        } else {
            log.warn("Invalid OTP provided for user: {}", user.getEmail());
            log.warn("Expected: '{}', Got: '{}'", user.getOtp(), otp);
        }
        
        return isValid;
    }
    
    @Override
    public void sendOTPToAllUnverifiedUsers() {
        List<User> unverifiedUsers = userRepository.findByVerificationStatus(false);
        
        log.info("Sending OTP to {} unverified users", unverifiedUsers.size());
        
        for (User user : unverifiedUsers) {
            try {
                String otp = generateOTP();
                sendOTPEmail(user, otp);
                log.info("OTP sent to: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send OTP to: {}", user.getEmail(), e);
            }
        }
        
        log.info("OTP sending process completed for all unverified users");
    }
    
    @Override
    public void resendOTP(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        if (user.isVerified()) {
            throw new RuntimeException("User is already verified");
        }
        
        String otp = generateOTP();
        sendOTPEmail(user, otp);
        
        log.info("OTP resent to: {}", email);
    }
} 