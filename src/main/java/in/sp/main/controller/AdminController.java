package in.sp.main.controller;

import in.sp.main.entity.User;
import in.sp.main.entity.Restaurant;
import in.sp.main.service.UserService;
import in.sp.main.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RestaurantService restaurantService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.ADMIN) {
                    model.addAttribute("user", user);
                    model.addAttribute("userInitial", user.getName().substring(0, 1).toUpperCase());
                    return "admin/dashboard";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/control-panel")
    public String controlPanel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.ADMIN) {
                    model.addAttribute("user", user);
                    model.addAttribute("userInitial", user.getName().substring(0, 1).toUpperCase());
                    return "admin/control-panel";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/restaurants")
    public String pendingRestaurants(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.ADMIN) {
                    try {
                        // Get all restaurants for admin review
                        List<Restaurant> allRestaurants = restaurantService.findAll();
                        List<Restaurant> pendingRestaurants = allRestaurants.stream()
                            .filter(r -> !r.isVerified())
                            .toList();
                        List<Restaurant> verifiedRestaurants = allRestaurants.stream()
                            .filter(Restaurant::isVerified)
                            .toList();
                        
                        model.addAttribute("pendingRestaurants", pendingRestaurants);
                        model.addAttribute("verifiedRestaurants", verifiedRestaurants);
                        model.addAttribute("totalRestaurants", allRestaurants.size());
                        model.addAttribute("pendingCount", pendingRestaurants.size());
                        model.addAttribute("verifiedCount", verifiedRestaurants.size());
                    } catch (Exception e) {
                        model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
                    }
                    model.addAttribute("user", user);
                    return "admin/restaurants";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/restaurants/verify")
    public String verifyRestaurant(@RequestParam Long restaurantId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.ADMIN) {
                    try {
                        var restaurantOpt = restaurantService.findById(restaurantId);
                        if (restaurantOpt.isPresent()) {
                            Restaurant restaurant = restaurantOpt.get();
                            restaurant.setVerified(true);
                            restaurantService.save(restaurant);
                            redirectAttributes.addFlashAttribute("success", "Restaurant '" + restaurant.getRestaurantName() + "' has been verified successfully!");
                        } else {
                            redirectAttributes.addFlashAttribute("error", "Restaurant not found!");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error verifying restaurant: " + e.getMessage());
                    }
                    return "redirect:/admin/restaurants";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/restaurants/reject")
    public String rejectRestaurant(@RequestParam Long restaurantId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.ADMIN) {
                    try {
                        var restaurantOpt = restaurantService.findById(restaurantId);
                        if (restaurantOpt.isPresent()) {
                            Restaurant restaurant = restaurantOpt.get();
                            restaurant.setActive(false);
                            restaurantService.save(restaurant);
                            redirectAttributes.addFlashAttribute("success", "Restaurant '" + restaurant.getRestaurantName() + "' has been rejected and deactivated!");
                        } else {
                            redirectAttributes.addFlashAttribute("error", "Restaurant not found!");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error rejecting restaurant: " + e.getMessage());
                    }
                    return "redirect:/admin/restaurants";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/users")
    public String adminUsers(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.ADMIN) {
                    try {
                        List<User> allUsers = userService.findAll();
                        List<User> adminUsers = allUsers.stream()
                            .filter(u -> u.getRole() == User.UserRole.ADMIN)
                            .toList();
                        List<User> restaurantOwners = allUsers.stream()
                            .filter(u -> u.getRole() == User.UserRole.RESTAURANT_OWNER)
                            .toList();
                        List<User> hotelOwners = allUsers.stream()
                            .filter(u -> u.getRole() == User.UserRole.HOTEL_OWNER)
                            .toList();
                        List<User> cloudKitchenOwners = allUsers.stream()
                            .filter(u -> u.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER)
                            .toList();
                        List<User> customers = allUsers.stream()
                            .filter(u -> u.getRole() == User.UserRole.CUSTOMER)
                            .toList();
                        
                        model.addAttribute("allUsers", allUsers);
                        model.addAttribute("adminUsers", adminUsers);
                        model.addAttribute("restaurantOwners", restaurantOwners);
                        model.addAttribute("hotelOwners", hotelOwners);
                        model.addAttribute("cloudKitchenOwners", cloudKitchenOwners);
                        model.addAttribute("customers", customers);
                        model.addAttribute("totalUsers", allUsers.size());
                        model.addAttribute("adminCount", adminUsers.size());
                        model.addAttribute("restaurantOwnerCount", restaurantOwners.size());
                        model.addAttribute("hotelOwnerCount", hotelOwners.size());
                        model.addAttribute("cloudKitchenOwnerCount", cloudKitchenOwners.size());
                        model.addAttribute("customerCount", customers.size());
                    } catch (Exception e) {
                        model.addAttribute("error", "Error loading users: " + e.getMessage());
                    }
                    model.addAttribute("user", user);
                    return "admin/users";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/users/make-admin")
    public String makeUserAdmin(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                if (currentUser.getRole() == User.UserRole.ADMIN) {
                    try {
                        var targetUserOpt = userService.findById(userId);
                        if (targetUserOpt.isPresent()) {
                            User targetUser = targetUserOpt.get();
                            targetUser.setRole(User.UserRole.ADMIN);
                            targetUser.setVerified(true);
                            userService.updateUser(targetUser);
                            redirectAttributes.addFlashAttribute("success", "User '" + targetUser.getName() + "' is now an ADMIN!");
                        } else {
                            redirectAttributes.addFlashAttribute("error", "Target user not found!");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error making user admin: " + e.getMessage());
                    }
                    return "redirect:/admin/users";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/users/remove-admin")
    public String removeUserAdmin(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                if (currentUser.getRole() == User.UserRole.ADMIN) {
                    try {
                        var targetUserOpt = userService.findById(userId);
                        if (targetUserOpt.isPresent()) {
                            User targetUser = targetUserOpt.get();
                            // Prevent removing the last admin
                            if (targetUser.getId().equals(currentUser.getId())) {
                                redirectAttributes.addFlashAttribute("error", "Cannot remove yourself as admin!");
                            } else {
                                targetUser.setRole(User.UserRole.CUSTOMER);
                                userService.updateUser(targetUser);
                                redirectAttributes.addFlashAttribute("success", "User '" + targetUser.getName() + "' is no longer an ADMIN!");
                            }
                        } else {
                            redirectAttributes.addFlashAttribute("error", "Target user not found!");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error removing admin: " + e.getMessage());
                    }
                    return "redirect:/admin/users";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
} 