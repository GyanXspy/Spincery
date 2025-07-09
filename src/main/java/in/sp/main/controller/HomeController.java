package in.sp.main.controller;

import in.sp.main.entity.User;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final UserService userService;
    
    /**
     * Handles the root ("/") GET request.
     * Loads the home page and, if the user is authenticated, adds user info to the model for display.
     * Retrieves the authenticated user's details and their initial for UI personalization.
     * Handles errors in user info retrieval gracefully.
     */
    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    model.addAttribute("user", user);
                    model.addAttribute("userInitial", user.getName().substring(0, 1).toUpperCase());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading user information: " + e.getMessage());
            }
        }
        return "index";
    }
    
    /**
     * Handles email verification via a GET request to "/verify".
     * Checks the provided token, verifies the user via the UserService, and redirects to login with a success or error message.
     * If the token is invalid or verification fails, an error message is shown.
     */
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam(required = false) String token, RedirectAttributes redirectAttributes) {
        if (token == null || token.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification token.");
            return "redirect:/login";
        }
        
        try {
            userService.verifyUser(token);
            redirectAttributes.addFlashAttribute("success", "Email verified successfully! You can now login.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification token: " + e.getMessage());
        }
        return "redirect:/login";
    }
} 