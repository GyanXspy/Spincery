package in.sp.main.controller;

import in.sp.main.entity.User;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TestController {
    
    private final UserService userService;
    
    @GetMapping("/test-verify")
    public String testVerifyPage(@RequestParam(required = false) String email, Model model) {
        log.info("Test verify page accessed with email: {}", email);
        model.addAttribute("email", email != null ? email : "test@example.com");
        return "verify-email";
    }
    
    @GetMapping("/test-otp")
    public String testOTPGeneration() {
        log.info("Testing OTP generation...");
        // This will just return a simple message
        return "redirect:/verify-email?email=test@example.com";
    }
    
    @GetMapping("/debug-otp")
    public String debugOTP(@RequestParam String email) {
        log.info("Debugging OTP for email: {}", email);
        
        try {
            var userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                log.info("User found: {}", user.getEmail());
                log.info("Stored OTP: '{}'", user.getOtp());
                log.info("OTP Expiry: {}", user.getOtpExpiry());
                log.info("Is Verified: {}", user.isVerified());
                
                return "redirect:/verify-email?email=" + email + "&debug=true";
            } else {
                log.warn("User not found: {}", email);
                return "redirect:/verify-email?email=" + email + "&error=User not found";
            }
        } catch (Exception e) {
            log.error("Error debugging OTP: {}", e.getMessage());
            return "redirect:/verify-email?email=" + email + "&error=" + e.getMessage();
        }
    }
} 