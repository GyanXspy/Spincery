package in.sp.main.controller;

import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OTPController {
    
    private final UserService userService;
    
    /**
     * Displays the OTP verification page
     */
    @GetMapping("/verify-email")
    public String verifyEmailPage(@RequestParam(required = false) String email, Model model) {
        log.info("Accessing verify-email page with email: {}", email);
        
        // Check for flash attributes
        if (model.containsAttribute("email")) {
            String flashEmail = (String) model.asMap().get("email");
            log.info("Found flash email attribute: {}", flashEmail);
            model.addAttribute("email", flashEmail);
        } else if (email != null && !email.trim().isEmpty()) {
            model.addAttribute("email", email);
        }
        
        log.info("Returning verify-email template");
        return "verify-email";
    }
    
    /**
     * Handles OTP verification form submission
     */
    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam("email") String email,
                             @RequestParam("otp") String otp,
                             RedirectAttributes redirectAttributes) {
        // Fix: handle multiple emails
        if (email != null && email.contains(",")) {
            email = email.split(",")[0].trim();
        }
        log.info("OTP verification attempt - Email: '{}', OTP: '{}'", email, otp);
        
        try {
            boolean isValid = userService.validateOTP(email, otp);
            log.info("OTP validation result: {}", isValid);
            
            if (isValid) {
                redirectAttributes.addFlashAttribute("success", "Email verified successfully! You can now login.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/verify-email";
            }
        } catch (Exception e) {
            log.error("Error during OTP verification: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error during verification: " + e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-email";
        }
    }
    
    /**
     * Handles OTP resend request
     */
    @PostMapping("/resend-otp")
    public String resendOTP(@RequestParam("email") String email,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.resendOTP(email);
            redirectAttributes.addFlashAttribute("success", "OTP resent successfully! Please check your email.");
            redirectAttributes.addFlashAttribute("email", email);
        } catch (Exception e) {
            log.error("Error resending OTP: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error resending OTP: " + e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
        }
        return "redirect:/verify-email";
    }
    
    /**
     * Admin endpoint to send OTP to all unverified users
     */
    @GetMapping("/admin/send-otp-to-all")
    public String sendOTPToAllUsers(RedirectAttributes redirectAttributes) {
        try {
            userService.sendOTPToAllUnverifiedUsers();
            redirectAttributes.addFlashAttribute("success", "OTP sent to all unverified users successfully!");
        } catch (Exception e) {
            log.error("Error sending OTP to all users: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error sending OTP to all users: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
} 