package in.sp.main.controller;

import in.sp.main.entity.User;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public String userProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                return "user/profile";
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User user, 
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> currentUserOpt = userService.findByEmail(auth.getName());
                if (currentUserOpt.isPresent()) {
                    User currentUser = currentUserOpt.get();
                    
                    // Update only allowed fields
                    currentUser.setName(user.getName());
                    currentUser.setPhone(user.getPhone());
                    currentUser.setAddress(user.getAddress());
                    
                    userService.updateUser(currentUser);
                    redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }
    
    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        return "user/change-password";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
                return "redirect:/user/change-password";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long!");
                return "redirect:/user/change-password";
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    boolean success = userService.changePassword(user.getId(), oldPassword, newPassword);
                    if (success) {
                        redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
                        return "redirect:/user/profile";
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Current password is incorrect!");
                    }
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
        }
        return "redirect:/user/change-password";
    }
    
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "user/reset-password";
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(email);
            redirectAttributes.addFlashAttribute("success", "Password reset link sent to your email!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sending reset link: " + e.getMessage());
        }
        return "redirect:/login";
    }
    
    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.verifyUser(token);
            redirectAttributes.addFlashAttribute("success", "Email verified successfully! Please login.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification token!");
        }
        return "redirect:/login";
    }
    
    // Admin endpoints for user management
    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.ADMIN) {
                List<User> users = userService.findAll();
                model.addAttribute("users", users);
                return "admin/users";
            }
        }
        return "redirect:/access-denied";
    }
    
    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> adminOpt = userService.findByEmail(auth.getName());
                if (adminOpt.isPresent() && adminOpt.get().getRole() == User.UserRole.ADMIN) {
                    userService.deleteUser(id);
                    redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/user/admin/users";
    }
} 