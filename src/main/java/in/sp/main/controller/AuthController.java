package in.sp.main.controller;

import in.sp.main.entity.User;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * Displays the login page.
     * Handles GET requests to "/login" and returns the login view.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    /**
     * Displays the registration page.
     * Handles GET requests to "/register" and prepares a new User object for the form.
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }
    
    /**
     * Handles user registration form submission.
     * Validates input fields, checks password and role, and registers the user via UserService.
     * On success, redirects to login; on error, returns to registration with error messages.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, 
                               @RequestParam("role") String role,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes, 
                               Model model) {
        try {
            // Validate required fields
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                model.addAttribute("error", "Name is required.");
                model.addAttribute("user", user);
                return "register";
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "Email is required.");
                model.addAttribute("user", user);
                return "register";
            }
            
            if (user.getPassword() == null || user.getPassword().isEmpty() || 
                confirmPassword == null || confirmPassword.isEmpty()) {
                model.addAttribute("error", "Password and Confirm Password are required.");
                model.addAttribute("user", user);
                return "register";
            }
            
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("user", user);
                return "register";
            }
            
            if (user.getPassword().length() < 6) {
                model.addAttribute("error", "Password must be at least 6 characters long.");
                model.addAttribute("user", user);
                return "register";
            }
            
            // Validate role
            try {
                user.setRole(User.UserRole.valueOf(role));
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Invalid role selected.");
                model.addAttribute("user", user);
                return "register";
            }
            
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }
    
    /**
     * Redirects authenticated users to their respective dashboards based on their role.
     * Handles GET requests to "/dashboard" and checks the user's role to determine the redirect target.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            var userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                switch (user.getRole()) {
                    case CUSTOMER:
                        return "redirect:/user/dashboard";
                    case RESTAURANT_OWNER:
                        return "redirect:/restaurant/dashboard";
                    case HOTEL_OWNER:
                        return "redirect:/hotel/dashboard";
                    case CLOUD_KITCHEN_OWNER:
                        return "redirect:/cloud-kitchen/dashboard";
                    case ADMIN:
                        return "redirect:/admin/dashboard";
                    default:
                        return "redirect:/user/dashboard";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Displays the dashboard for users with the CUSTOMER role.
     * Handles GET requests to "/user/dashboard" and adds user info to the model.
     * Redirects to access denied if the user is not a CUSTOMER.
     */
    @GetMapping("/user/dashboard")
    public String userDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            var userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CUSTOMER) {
                    model.addAttribute("user", user);
                    return "user/dashboard";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Development endpoint to verify all users.
     * Sets all users as verified for development/testing purposes.
     * Only accessible in development environments.
     */
    @GetMapping("/dev/verify-all-users")
    public String verifyAllUsers(RedirectAttributes redirectAttributes) {
        try {
            userService.findAll().forEach(user -> {
                if (!user.isVerified()) {
                    user.setVerified(true);
                    userService.updateUser(user);
                }
            });
            redirectAttributes.addFlashAttribute("success", "All users have been verified for development!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error verifying users: " + e.getMessage());
        }
        return "redirect:/login";
    }
    
    /**
     * Development endpoint to create an initial admin user.
     * Only available in development mode and if no admin exists.
     * Registers a new admin user with the provided credentials.
     */
    @GetMapping("/dev/create-admin")
    public String createInitialAdmin(@RequestParam String email, 
                                   @RequestParam String password,
                                   @RequestParam String name,
                                   RedirectAttributes redirectAttributes) {
        // Only allow in development environment
        String profile = System.getProperty("spring.profiles.active", "default");
        if (!"dev".equals(profile) && !"development".equals(profile)) {
            redirectAttributes.addFlashAttribute("error", "This endpoint is only available in development mode!");
            return "redirect:/login";
        }
        
        try {
            // Check if admin already exists
            var existingAdmin = userService.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .findFirst();
            
            if (existingAdmin.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Admin already exists! Use database to create additional admins.");
                return "redirect:/login";
            }
            
            // Create admin user
            User admin = new User();
            admin.setName(name);
            admin.setEmail(email);
            admin.setPassword(password);
            admin.setRole(User.UserRole.ADMIN);
            admin.setVerified(true);
            admin.setPhone("0000000000"); // Default phone
            admin.setAddress("Admin Address");
            admin.setCity("Admin City");
            admin.setState("Admin State");
            admin.setZipCode("000000");
            
            userService.registerUser(admin);
            redirectAttributes.addFlashAttribute("success", "Admin user created successfully! Login with: " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating admin: " + e.getMessage());
        }
        return "redirect:/login";
    }
} 