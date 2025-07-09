package in.sp.main.service;

import in.sp.main.entity.User;

public interface OTPService {
    
    /**
     * Generates a 6-digit OTP
     * @return 6-digit OTP string
     */
    String generateOTP();
    
    /**
     * Sends OTP to user's email
     * @param user User to send OTP to
     * @param otp OTP to send
     */
    void sendOTPEmail(User user, String otp);
    
    /**
     * Validates OTP for a user
     * @param user User to validate OTP for
     * @param otp OTP to validate
     * @return true if OTP is valid, false otherwise
     */
    boolean validateOTP(User user, String otp);
    
    /**
     * Sends OTP to all unverified users
     */
    void sendOTPToAllUnverifiedUsers();
    
    /**
     * Resends OTP to a specific user
     * @param email User's email
     */
    void resendOTP(String email);
} 