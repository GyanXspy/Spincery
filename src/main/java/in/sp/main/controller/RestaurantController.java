package in.sp.main.controller;

import in.sp.main.entity.Restaurant;
import in.sp.main.entity.User;
import in.sp.main.entity.FoodOrder;
import in.sp.main.entity.MenuItem;
import in.sp.main.service.RestaurantService;
import in.sp.main.service.UserService;
import in.sp.main.service.FoodOrderService;
import in.sp.main.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final FoodOrderService foodOrderService;
    private final MenuItemService menuItemService;
    
    @GetMapping("/dashboard")
    public String restaurantDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    model.addAttribute("user", user);
                    String userInitial = (user.getName() != null && !user.getName().isEmpty()) ? user.getName().substring(0, 1).toUpperCase() : "";
                    model.addAttribute("userInitial", userInitial);
                    return "restaurant/dashboard";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/register")
    public String restaurantRegister(Model model) {
        if (!model.containsAttribute("restaurant")) {
            model.addAttribute("restaurant", new Restaurant());
        }
        return "restaurant/register";
    }

    @PostMapping("/register")
    public String handleRestaurantRegister(@Valid @ModelAttribute("restaurant") Restaurant restaurant,
                                           BindingResult bindingResult,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("error", "Please correct the errors in the form.");
            return "restaurant/register";
        }
        try {
            restaurantService.save(restaurant);
            redirectAttributes.addFlashAttribute("success", "Restaurant registered successfully!");
            return "redirect:/restaurant/register";
        } catch (Exception e) {
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("error", "Error registering restaurant: " + e.getMessage());
            return "restaurant/register";
        }
    }
    
    @GetMapping("/list")
    public String restaurantList(@RequestParam(required = false) String city, Model model) {
        List<Restaurant> restaurants;
        try {
            if (city != null && !city.trim().isEmpty()) {
                restaurants = restaurantService.findActiveVerifiedByCity(city);
            } else {
                restaurants = restaurantService.findByIsVerifiedTrue();
            }
        } catch (Exception e) {
            restaurants = new ArrayList<>();
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
        }
        
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("city", city != null ? city : "");
        return "restaurant/list";
    }
    
    @GetMapping("/menu")
    public String restaurantMenu(@RequestParam(required = false) Long restaurantId, Model model) {
        if (restaurantId == null) {
            return "redirect:/restaurant/list";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            model.addAttribute("restaurant", restaurant);
            
            // Add menu items to the model
            try {
                List<MenuItem> menuItems = menuItemService.findByRestaurantId(restaurantId);
                model.addAttribute("menuItems", menuItems);
            } catch (Exception e) {
                model.addAttribute("menuItems", new ArrayList<>());
                model.addAttribute("error", "Error loading menu items: " + e.getMessage());
            }
            
            return "restaurant/menu";
        } else {
            model.addAttribute("restaurant", null);
            model.addAttribute("menuItems", new ArrayList<>());
            model.addAttribute("error", "Restaurant not found");
            return "restaurant/menu";
        }
    }
    
    @GetMapping("/orders")
    public String restaurantOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        // Get the restaurant owned by this user
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0); // Get the first restaurant
                            List<FoodOrder> orders = foodOrderService.findByRestaurantId(restaurant.getId());
                            model.addAttribute("orders", orders);
                            model.addAttribute("restaurant", restaurant);
                        } else {
                            model.addAttribute("orders", new ArrayList<>());
                            model.addAttribute("restaurant", null);
                            model.addAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        model.addAttribute("orders", new ArrayList<>());
                        model.addAttribute("restaurant", null);
                        model.addAttribute("error", "Error loading orders: " + e.getMessage());
                    }
                    return "restaurant/orders";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/analytics")
    public String restaurantAnalytics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        // Get the restaurant owned by this user
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0);
                            
                            // Get orders for analytics
                            List<FoodOrder> orders = foodOrderService.findByRestaurantId(restaurant.getId());
                            
                            // Calculate analytics data
                            long totalOrders = orders.size();
                            long pendingOrders = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.PENDING).count();
                            long confirmedOrders = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.CONFIRMED).count();
                            long preparingOrders = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.PREPARING).count();
                            long deliveredOrders = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.DELIVERED).count();
                            long cancelledOrders = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.CANCELLED).count();
                            
                            double totalRevenue = orders.stream()
                                .filter(o -> o.getStatus() == FoodOrder.OrderStatus.DELIVERED)
                                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                                .sum();
                            
                            double avgOrderValue = deliveredOrders > 0 ? totalRevenue / deliveredOrders : 0.0;
                            
                            // Calculate rating
                            double avgRating = orders.stream()
                                .filter(o -> o.getRating() != null)
                                .mapToDouble(FoodOrder::getRating)
                                .average()
                                .orElse(0.0);
                            
                            // Get recent orders (last 10)
                            List<FoodOrder> recentOrders = orders.stream()
                                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                                .limit(10)
                                .toList();
                            
                            // Calculate unique customers
                            long totalCustomers = orders.stream()
                                .map(FoodOrder::getUser)
                                .distinct()
                                .count();
                            
                            // Calculate growth percentages (placeholder for now)
                            String orderGrowth = "+12%";
                            String revenueGrowth = "+8%";
                            String ratingGrowth = "+0.2";
                            String customerGrowth = "+15%";
                            
                            model.addAttribute("restaurant", restaurant);
                            model.addAttribute("orders", orders);
                            model.addAttribute("recentOrders", recentOrders);
                            model.addAttribute("totalOrders", totalOrders);
                            model.addAttribute("pendingOrders", pendingOrders);
                            model.addAttribute("confirmedOrders", confirmedOrders);
                            model.addAttribute("preparingOrders", preparingOrders);
                            model.addAttribute("deliveredOrders", deliveredOrders);
                            model.addAttribute("cancelledOrders", cancelledOrders);
                            model.addAttribute("totalRevenue", totalRevenue);
                            model.addAttribute("avgOrderValue", avgOrderValue);
                            model.addAttribute("avgRating", avgRating);
                            model.addAttribute("totalCustomers", totalCustomers);
                            model.addAttribute("orderGrowth", orderGrowth);
                            model.addAttribute("revenueGrowth", revenueGrowth);
                            model.addAttribute("ratingGrowth", ratingGrowth);
                            model.addAttribute("customerGrowth", customerGrowth);
                            model.addAttribute("popularItems", new ArrayList<>()); // Placeholder for popular items
                        } else {
                            model.addAttribute("restaurant", null);
                            model.addAttribute("error", "No restaurant found for this user");
                            model.addAttribute("totalOrders", 0);
                            model.addAttribute("pendingOrders", 0);
                            model.addAttribute("confirmedOrders", 0);
                            model.addAttribute("preparingOrders", 0);
                            model.addAttribute("deliveredOrders", 0);
                            model.addAttribute("cancelledOrders", 0);
                            model.addAttribute("totalRevenue", 0.0);
                            model.addAttribute("avgOrderValue", 0.0);
                            model.addAttribute("avgRating", 0.0);
                            model.addAttribute("totalCustomers", 0);
                            model.addAttribute("orderGrowth", "+0%");
                            model.addAttribute("revenueGrowth", "+0%");
                            model.addAttribute("ratingGrowth", "+0.0");
                            model.addAttribute("customerGrowth", "+0%");
                            model.addAttribute("recentOrders", new ArrayList<>());
                            model.addAttribute("popularItems", new ArrayList<>());
                        }
                    } catch (Exception e) {
                        model.addAttribute("restaurant", null);
                        model.addAttribute("error", "Error loading analytics: " + e.getMessage());
                        model.addAttribute("totalOrders", 0);
                        model.addAttribute("pendingOrders", 0);
                        model.addAttribute("confirmedOrders", 0);
                        model.addAttribute("preparingOrders", 0);
                        model.addAttribute("deliveredOrders", 0);
                        model.addAttribute("cancelledOrders", 0);
                        model.addAttribute("totalRevenue", 0.0);
                        model.addAttribute("avgOrderValue", 0.0);
                        model.addAttribute("avgRating", 0.0);
                        model.addAttribute("totalCustomers", 0);
                        model.addAttribute("orderGrowth", "+0%");
                        model.addAttribute("revenueGrowth", "+0%");
                        model.addAttribute("ratingGrowth", "+0.0");
                        model.addAttribute("customerGrowth", "+0%");
                        model.addAttribute("recentOrders", new ArrayList<>());
                        model.addAttribute("popularItems", new ArrayList<>());
                    }
                    model.addAttribute("user", user);
                    return "restaurant/analytics";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/settings")
    public String restaurantSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        // Get the restaurant owned by this user
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0);
                            model.addAttribute("restaurant", restaurant);
                        } else {
                            model.addAttribute("restaurant", null);
                            model.addAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        model.addAttribute("restaurant", null);
                        model.addAttribute("error", "Error loading restaurant: " + e.getMessage());
                    }
                    model.addAttribute("user", user);
                    return "restaurant/settings";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/hours")
    public String restaurantHours(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0);
                            model.addAttribute("restaurant", restaurant);
                        } else {
                            model.addAttribute("restaurant", null);
                            model.addAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        model.addAttribute("restaurant", null);
                        model.addAttribute("error", "Error loading restaurant: " + e.getMessage());
                    }
                    model.addAttribute("user", user);
                    return "restaurant/hours";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/delivery")
    public String restaurantDelivery(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0);
                            model.addAttribute("restaurant", restaurant);
                        } else {
                            model.addAttribute("restaurant", null);
                            model.addAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        model.addAttribute("restaurant", null);
                        model.addAttribute("error", "Error loading restaurant: " + e.getMessage());
                    }
                    model.addAttribute("user", user);
                    return "restaurant/delivery";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/hours/update")
    public String updateRestaurantHours(RedirectAttributes redirectAttributes,
                                       Model model,
                                       @RequestParam(required = false) String openingTime,
                                       @RequestParam(required = false) String closingTime,
                                       @RequestParam(required = false) String workingDays,
                                       @RequestParam(required = false) String weeklyOff) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0);
                            
                            // Update hours
                            if (openingTime != null && !openingTime.trim().isEmpty()) {
                                try {
                                    LocalTime openingTimeObj = LocalTime.parse(openingTime);
                                    restaurant.setOpeningTime(openingTimeObj);
                                } catch (DateTimeParseException e) {
                                    redirectAttributes.addFlashAttribute("error", "Invalid opening time format. Use HH:MM");
                                    return "redirect:/restaurant/hours";
                                }
                            }
                            if (closingTime != null && !closingTime.trim().isEmpty()) {
                                try {
                                    LocalTime closingTimeObj = LocalTime.parse(closingTime);
                                    restaurant.setClosingTime(closingTimeObj);
                                } catch (DateTimeParseException e) {
                                    redirectAttributes.addFlashAttribute("error", "Invalid closing time format. Use HH:MM");
                                    return "redirect:/restaurant/hours";
                                }
                            }
                            if (workingDays != null && !workingDays.trim().isEmpty()) {
                                restaurant.setWorkingDays(workingDays);
                            }
                            if (weeklyOff != null && !weeklyOff.trim().isEmpty()) {
                                restaurant.setWeeklyOff(weeklyOff);
                            }
                            
                            restaurantService.save(restaurant);
                            redirectAttributes.addFlashAttribute("success", "Operating hours updated successfully!");
                        } else {
                            redirectAttributes.addFlashAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error updating hours: " + e.getMessage());
                    }
                    return "redirect:/restaurant/hours";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/delivery/update")
    public String updateRestaurantDelivery(RedirectAttributes redirectAttributes,
                                          Model model,
                                          @RequestParam(required = false) Boolean deliveryOffered,
                                          @RequestParam(required = false) Double deliveryRadius,
                                          @RequestParam(required = false) Double packagingCharges,
                                          @RequestParam(required = false) Integer avgPreparationTime) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0);
                            
                            // Update delivery settings
                            if (deliveryOffered != null) {
                                restaurant.setDeliveryOffered(deliveryOffered);
                            }
                            if (deliveryRadius != null) {
                                restaurant.setDeliveryRadius(deliveryRadius);
                            }
                            if (packagingCharges != null) {
                                restaurant.setPackagingCharges(packagingCharges);
                            }
                            if (avgPreparationTime != null) {
                                restaurant.setAvgPreparationTime(avgPreparationTime);
                            }
                            
                            restaurantService.save(restaurant);
                            redirectAttributes.addFlashAttribute("success", "Delivery settings updated successfully!");
                        } else {
                            redirectAttributes.addFlashAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error updating delivery settings: " + e.getMessage());
                    }
                    return "redirect:/restaurant/delivery";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/settings/update")
    public String updateRestaurantSettings(RedirectAttributes redirectAttributes,
                                          Model model,
                                          @RequestParam(required = false) String restaurantName,
                                          @RequestParam(required = false) String ownerName,
                                          @RequestParam(required = false) String email,
                                          @RequestParam(required = false) String phone,
                                          @RequestParam(required = false) String whatsappNumber,
                                          @RequestParam(required = false) String alternateContact,
                                          @RequestParam(required = false) String address,
                                          @RequestParam(required = false) String city,
                                          @RequestParam(required = false) String state,
                                          @RequestParam(required = false) String zipCode,
                                          @RequestParam(required = false) String nearbyLandmark,
                                          @RequestParam(required = false) String businessRegistrationNumber,
                                          @RequestParam(required = false) String fssaiLicenseNumber,
                                          @RequestParam(required = false) String gstin,
                                          @RequestParam(required = false) String description,
                                          @RequestParam(required = false) String workingDays,
                                          @RequestParam(required = false) String weeklyOff,
                                          @RequestParam(required = false) String openingTime,
                                          @RequestParam(required = false) String closingTime,
                                          @RequestParam(required = false) Boolean deliveryOffered,
                                          @RequestParam(required = false) Double deliveryRadius,
                                          @RequestParam(required = false) Double packagingCharges,
                                          @RequestParam(required = false) Integer avgPreparationTime) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        // Ensure the restaurant belongs to the current user
                        List<Restaurant> userRestaurants = restaurantService.findByOwnerId(user.getId());
                        if (!userRestaurants.isEmpty()) {
                            Restaurant existingRestaurant = userRestaurants.get(0);
                            
                            // Update the existing restaurant with new data
                            if (restaurantName != null) existingRestaurant.setRestaurantName(restaurantName);
                            if (ownerName != null) existingRestaurant.setOwnerName(ownerName);
                            if (email != null) existingRestaurant.setEmail(email);
                            if (phone != null) existingRestaurant.setPhone(phone);
                            if (whatsappNumber != null) existingRestaurant.setWhatsappNumber(whatsappNumber);
                            if (alternateContact != null) existingRestaurant.setAlternateContact(alternateContact);
                            if (address != null) existingRestaurant.setAddress(address);
                            if (city != null) existingRestaurant.setCity(city);
                            if (state != null) existingRestaurant.setState(state);
                            if (zipCode != null) existingRestaurant.setZipCode(zipCode);
                            if (nearbyLandmark != null) existingRestaurant.setNearbyLandmark(nearbyLandmark);
                            if (businessRegistrationNumber != null) existingRestaurant.setBusinessRegistrationNumber(businessRegistrationNumber);
                            if (fssaiLicenseNumber != null) existingRestaurant.setFssaiLicenseNumber(fssaiLicenseNumber);
                            if (gstin != null) existingRestaurant.setGstin(gstin);
                            if (description != null) existingRestaurant.setDescription(description);
                            if (workingDays != null) existingRestaurant.setWorkingDays(workingDays);
                            if (weeklyOff != null) existingRestaurant.setWeeklyOff(weeklyOff);
                            if (deliveryOffered != null) existingRestaurant.setDeliveryOffered(deliveryOffered);
                            if (deliveryRadius != null) existingRestaurant.setDeliveryRadius(deliveryRadius);
                            if (packagingCharges != null) existingRestaurant.setPackagingCharges(packagingCharges);
                            if (avgPreparationTime != null) existingRestaurant.setAvgPreparationTime(avgPreparationTime);
                            
                            // Parse time strings to LocalTime
                            if (openingTime != null && !openingTime.trim().isEmpty()) {
                                try {
                                    LocalTime parsedOpeningTime = LocalTime.parse(openingTime, DateTimeFormatter.ofPattern("HH:mm"));
                                    existingRestaurant.setOpeningTime(parsedOpeningTime);
                                } catch (DateTimeParseException e) {
                                    // Keep existing time if parsing fails
                                }
                            }
                            
                            if (closingTime != null && !closingTime.trim().isEmpty()) {
                                try {
                                    LocalTime parsedClosingTime = LocalTime.parse(closingTime, DateTimeFormatter.ofPattern("HH:mm"));
                                    existingRestaurant.setClosingTime(parsedClosingTime);
                                } catch (DateTimeParseException e) {
                                    // Keep existing time if parsing fails
                                }
                            }
                            
                            restaurantService.save(existingRestaurant);
                            redirectAttributes.addFlashAttribute("success", "Restaurant settings updated successfully!");
                        } else {
                            redirectAttributes.addFlashAttribute("error", "No restaurant found for this user");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Error updating restaurant settings: " + e.getMessage());
                    }
                    return "redirect:/restaurant/settings";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/edit")
    public String restaurantEdit(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("[DEBUG] Logged-in user ID: " + user.getId() + ", role: " + user.getRole());
                List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                if (!restaurants.isEmpty()) {
                    Restaurant restaurant = restaurants.get(0);
                    System.out.println("[DEBUG] Restaurant owner ID: " + (restaurant.getOwner() != null ? restaurant.getOwner().getId() : "null"));
                    model.addAttribute("restaurant", restaurant);
                    return "restaurant/edit";
                } else {
                    System.out.println("[DEBUG] No restaurant found for user ID: " + user.getId());
                    model.addAttribute("error", "No restaurant found for this user");
                    return "redirect:/restaurant/list";
                }
            }
        }
        return "redirect:/login";
    }
    
    @PostMapping("/edit")
    public String updateRestaurant(@Valid @ModelAttribute("restaurant") Restaurant restaurant,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    if (bindingResult.hasErrors()) {
                        model.addAttribute("restaurant", restaurant);
                        model.addAttribute("user", user);
                        model.addAttribute("error", "Please correct the errors in the form.");
                        return "restaurant/edit";
                    }
                    
                    try {
                        // Verify restaurant ownership
                        List<Restaurant> userRestaurants = restaurantService.findByOwnerId(user.getId());
                        if (!userRestaurants.isEmpty() && userRestaurants.get(0).getId().equals(restaurant.getId())) {
                            restaurantService.save(restaurant);
                            redirectAttributes.addFlashAttribute("success", "Restaurant updated successfully!");
                            return "redirect:/restaurant/edit";
                        } else {
                            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this restaurant!");
                        }
                    } catch (Exception e) {
                        model.addAttribute("restaurant", restaurant);
                        model.addAttribute("user", user);
                        model.addAttribute("error", "Error updating restaurant: " + e.getMessage());
                        return "restaurant/edit";
                    }
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
} 