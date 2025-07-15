package in.sp.main.controller;

import in.sp.main.entity.Restaurant;
import in.sp.main.entity.User;
import in.sp.main.entity.FoodOrder;
import in.sp.main.entity.MenuItem;
import in.sp.main.entity.OrderItem;
import in.sp.main.service.RestaurantService;
import in.sp.main.service.UserService;
import in.sp.main.service.FoodOrderService;
import in.sp.main.service.MenuItemService;
import in.sp.main.service.CloudinaryService;
import in.sp.main.service.OrderItemService;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final FoodOrderService foodOrderService;
    private final MenuItemService menuItemService;
    private final CloudinaryService cloudinaryService;
    private final OrderItemService orderItemService;
    
    /**
     * Displays the restaurant dashboard for the authenticated owner.
     * Loads user info and their restaurants for the dashboard view.
     */
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
    
    /**
     * Displays the restaurant registration form.
     * Prepares a new Restaurant object for the form.
     */
    @GetMapping("/register")
    public String restaurantRegister(Model model) {
        if (!model.containsAttribute("restaurant")) {
            model.addAttribute("restaurant", new Restaurant());
        }
        return "restaurant/register";
    }

    /**
     * Handles restaurant registration form submission.
     * Associates the restaurant with the authenticated user and uploads the logo image.
     */
    @PostMapping("/register")
    public String registerRestaurant(@ModelAttribute Restaurant restaurant,
                                     @RequestParam("imageFile") MultipartFile imageFile,
                                     Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                restaurant.setOwner(user);
                restaurant.setOwnerName(user.getName());
                restaurant.setEmail(user.getEmail());
                restaurant.setPhone(user.getPhone());
            } else {
                model.addAttribute("error", "Could not determine owner for this restaurant.");
                return "restaurant/register";
            }
            String imageUrl = cloudinaryService.uploadFile(imageFile, "restaurant");
            restaurant.setLogoUrl(imageUrl);
            restaurant.setCoverPhotoUrl(imageUrl);
            restaurantService.save(restaurant);
            model.addAttribute("success", "Restaurant registered successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error registering Restaurant: " + e.getMessage());
        }
        return "restaurant/register";
    }
    
    /**
     * Displays a list of restaurants, optionally filtered by city.
     * Adds the restaurants and city info to the model.
     */
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
    
    /**
     * Displays the menu for a specific restaurant by its ID.
     * Adds the restaurant and its menu items to the model.
     */
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
    
    /**
     * Displays the list of orders for the authenticated restaurant owner.
     * Loads all orders for the owner's restaurant.
     */
    @GetMapping("/orders")
    public String restaurantOrders(@RequestParam(value = "status", required = false) String status, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        if (!restaurants.isEmpty()) {
                            Restaurant restaurant = restaurants.get(0); // Get the first restaurant
                            List<FoodOrder> orders = foodOrderService.findByRestaurantId(restaurant.getId());
                            List<FoodOrder> filteredOrders = orders;
                            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
                                filteredOrders = orders.stream()
                                    .filter(o -> o.getStatus().name().equalsIgnoreCase(status))
                                    .toList();
                            }
                            model.addAttribute("orders", filteredOrders);
                            model.addAttribute("restaurant", restaurant);
                            model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");
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
    
    /**
     * Displays the analytics page for the authenticated restaurant owner.
     * Loads order statistics and analytics data for the owner's restaurant.
     */
    @GetMapping("/analytics")
    public String restaurantAnalytics(Model model) {
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
                            List<FoodOrder> orders = foodOrderService.findByRestaurantId(restaurant.getId());
                            List<OrderItem> orderItems = orderItemService.findByRestaurantId(restaurant.getId());
                            
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
                            
                            // --- Revenue trend (last 6 months) ---
                            java.time.YearMonth now = java.time.YearMonth.now();
                            List<Double> revenueTrend = new java.util.ArrayList<>();
                            for (int i = 5; i >= 0; i--) {
                                java.time.YearMonth ym = now.minusMonths(i);
                                double monthRevenue = orders.stream()
                                    .filter(o -> o.getStatus() == FoodOrder.OrderStatus.DELIVERED &&
                                        o.getCreatedAt() != null &&
                                        java.time.YearMonth.from(o.getCreatedAt()).equals(ym))
                                    .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                                    .sum();
                                revenueTrend.add(monthRevenue);
                            }
                            // --- Orders by status ---
                            long pending = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.PENDING).count();
                            long confirmed = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.CONFIRMED).count();
                            long preparing = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.PREPARING).count();
                            long ready = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.READY_FOR_PICKUP).count();
                            long delivered = orders.stream().filter(o -> o.getStatus() == FoodOrder.OrderStatus.DELIVERED).count();
                            List<Long> ordersByStatus = java.util.Arrays.asList(pending, confirmed, preparing, ready, delivered);
                            // --- Popular items (top 2 by order count) ---
                            java.util.Map<Long, Long> itemOrderCounts = new java.util.HashMap<>();
                            for (OrderItem oi : orderItems) {
                                Long menuItemId = oi.getMenuItem().getId();
                                itemOrderCounts.put(menuItemId, itemOrderCounts.getOrDefault(menuItemId, 0L) + 1);
                            }
                            List<MenuItem> menuItems = menuItemService.findByRestaurantId(restaurant.getId());
                            List<java.util.Map<String, Object>> popularItems = menuItems.stream()
                                .filter(mi -> itemOrderCounts.containsKey(mi.getId()))
                                .sorted((a, b) -> Long.compare(itemOrderCounts.get(b.getId()), itemOrderCounts.get(a.getId())))
                                .limit(2)
                                .map(mi -> {
                                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                                    map.put("name", mi.getDishName());
                                    map.put("price", mi.getPrice());
                                    map.put("orders", itemOrderCounts.get(mi.getId()));
                                    map.put("growth", 10); // Dummy growth
                                    return map;
                                })
                                .toList();
                            // --- Recent orders (last 2) ---
                            List<java.util.Map<String, Object>> recentOrdersList = orders.stream()
                                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                                .limit(2)
                                .map(o -> {
                                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                                    map.put("id", o.getOrderNumber());
                                    map.put("items", o.getOrderItems() != null ? o.getOrderItems().size() : 0);
                                    map.put("amount", o.getTotalAmount() != null ? o.getTotalAmount() : 0.0);
                                    String status = o.getStatus().name();
                                    map.put("status", status.substring(0,1) + status.substring(1).toLowerCase());
                                    String statusClass = switch (o.getStatus()) {
                                        case DELIVERED -> "bg-green-100 text-green-800";
                                        case CONFIRMED -> "bg-blue-100 text-blue-800";
                                        case PREPARING -> "bg-yellow-100 text-yellow-800";
                                        case PENDING -> "bg-gray-100 text-gray-800";
                                        default -> "bg-gray-100 text-gray-800";
                                    };
                                    map.put("statusClass", statusClass);
                                    return map;
                                })
                                .toList();
                            
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
                            model.addAttribute("popularItems", popularItems);
                            model.addAttribute("revenueTrend", revenueTrend);
                            model.addAttribute("ordersByStatus", ordersByStatus);
                            model.addAttribute("recentOrders", recentOrdersList);
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
    
    /**
     * Displays the restaurant settings page for the authenticated owner.
     * Loads the restaurant details for the settings view.
     */
    @GetMapping("/settings")
    public String restaurantSettings(@RequestParam(value = "restaurantId", required = false) Long restaurantId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        model.addAttribute("restaurants", restaurants);
                        Restaurant restaurant = null;
                        if (restaurantId != null) {
                            for (Restaurant r : restaurants) {
                                if (r.getId().equals(restaurantId)) {
                                    restaurant = r;
                                    break;
                                }
                            }
                        }
                        if (restaurant == null && !restaurants.isEmpty()) {
                            restaurant = restaurants.get(0);
                        }
                        model.addAttribute("restaurant", restaurant);
                        if (restaurants.isEmpty()) {
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
    
    /**
     * Displays the restaurant hours management page for the authenticated owner.
     * Loads the restaurant details for the hours management view.
     */
    @GetMapping("/hours")
    public String restaurantHours(@RequestParam(value = "restaurantId", required = false) Long restaurantId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    try {
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(user.getId());
                        model.addAttribute("restaurants", restaurants);
                        Restaurant restaurant = null;
                        if (restaurantId != null) {
                            for (Restaurant r : restaurants) {
                                if (r.getId().equals(restaurantId)) {
                                    restaurant = r;
                                    break;
                                }
                            }
                        }
                        if (restaurant == null && !restaurants.isEmpty()) {
                            restaurant = restaurants.get(0);
                        }
                        model.addAttribute("restaurant", restaurant);
                        if (restaurants.isEmpty()) {
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
    
    /**
     * Displays the restaurant delivery settings page for the authenticated owner.
     * Loads the restaurant details for the delivery settings view.
     */
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
    
    /**
     * Handles the update of restaurant operating hours.
     * Validates time formats and updates the restaurant's operating hours.
     */
    @PostMapping("/hours/update")
    public String updateRestaurantHours(@RequestParam(value = "restaurantId", required = false) Long restaurantId,
                                       RedirectAttributes redirectAttributes,
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
                        Restaurant restaurant = null;
                        if (restaurantId != null) {
                            for (Restaurant r : restaurants) {
                                if (r.getId().equals(restaurantId)) {
                                    restaurant = r;
                                    break;
                                }
                            }
                        }
                        if (restaurant == null && !restaurants.isEmpty()) {
                            restaurant = restaurants.get(0);
                        }
                        if (restaurant != null) {
                            // Update hours
                            if (openingTime != null && !openingTime.trim().isEmpty()) {
                                try {
                                    LocalTime openingTimeObj = LocalTime.parse(openingTime);
                                    restaurant.setOpeningTime(openingTimeObj);
                                } catch (DateTimeParseException e) {
                                    redirectAttributes.addFlashAttribute("error", "Invalid opening time format. Use HH:MM");
                                    return "redirect:/restaurant/hours?restaurantId=" + restaurant.getId();
                                }
                            }
                            if (closingTime != null && !closingTime.trim().isEmpty()) {
                                try {
                                    LocalTime closingTimeObj = LocalTime.parse(closingTime);
                                    restaurant.setClosingTime(closingTimeObj);
                                } catch (DateTimeParseException e) {
                                    redirectAttributes.addFlashAttribute("error", "Invalid closing time format. Use HH:MM");
                                    return "redirect:/restaurant/hours?restaurantId=" + restaurant.getId();
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
                    return "redirect:/restaurant/hours?restaurantId=" + (restaurantId != null ? restaurantId : "");
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Handles the update of restaurant delivery settings.
     * Updates delivery options, radius, packaging charges, and preparation time.
     */
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
    
    /**
     * Handles the comprehensive update of restaurant settings.
     * Updates all restaurant details including contact info, address, business details, and operational settings.
     */
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
    
    /**
     * Displays the restaurant edit form for the authenticated owner.
     * Loads the restaurant details for editing and checks ownership.
     */
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
    
    /**
     * Handles the restaurant edit form submission.
     * Validates input, checks ownership, and updates the restaurant with new image if provided.
     */
    @PostMapping("/edit")
    public String updateRestaurant(@Valid @ModelAttribute("restaurant") Restaurant restaurant,
                                   BindingResult bindingResult,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
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
                        Optional<Restaurant> dbRestaurantOpt = restaurantService.findById(restaurant.getId());
                        if (dbRestaurantOpt.isPresent() && dbRestaurantOpt.get().getOwner() != null && dbRestaurantOpt.get().getOwner().getId().equals(user.getId())) {
                            Restaurant dbRestaurant = dbRestaurantOpt.get();
                            // Update allowed fields
                            dbRestaurant.setRestaurantName(restaurant.getRestaurantName());
                            dbRestaurant.setOwnerName(restaurant.getOwnerName());
                            dbRestaurant.setPhone(restaurant.getPhone());
                            dbRestaurant.setWhatsappNumber(restaurant.getWhatsappNumber());
                            dbRestaurant.setAlternateContact(restaurant.getAlternateContact());
                            dbRestaurant.setBusinessRegistrationNumber(restaurant.getBusinessRegistrationNumber());
                            dbRestaurant.setAddress(restaurant.getAddress());
                            dbRestaurant.setCity(restaurant.getCity());
                            dbRestaurant.setState(restaurant.getState());
                            dbRestaurant.setZipCode(restaurant.getZipCode());
                            dbRestaurant.setNearbyLandmark(restaurant.getNearbyLandmark());
                            dbRestaurant.setFssaiLicenseNumber(restaurant.getFssaiLicenseNumber());
                            dbRestaurant.setGstin(restaurant.getGstin());
                            dbRestaurant.setDescription(restaurant.getDescription());
                            dbRestaurant.setWorkingDays(restaurant.getWorkingDays());
                            dbRestaurant.setWeeklyOff(restaurant.getWeeklyOff());
                            dbRestaurant.setOpeningTime(restaurant.getOpeningTime());
                            dbRestaurant.setClosingTime(restaurant.getClosingTime());
                            dbRestaurant.setDeliveryRadius(restaurant.getDeliveryRadius());
                            dbRestaurant.setPackagingCharges(restaurant.getPackagingCharges());
                            dbRestaurant.setAvgPreparationTime(restaurant.getAvgPreparationTime());
                            dbRestaurant.setDeliveryOffered(restaurant.isDeliveryOffered());
                            // Handle image upload if a new file is provided
                            if (imageFile != null && !imageFile.isEmpty()) {
                                String imageUrl = cloudinaryService.uploadFile(imageFile, "restaurant");
                                dbRestaurant.setCoverPhotoUrl(imageUrl);
                                dbRestaurant.setLogoUrl(imageUrl);
                            } else if ((dbRestaurant.getCoverPhotoUrl() == null || dbRestaurant.getCoverPhotoUrl().isBlank()) && dbRestaurant.getLogoUrl() != null && !dbRestaurant.getLogoUrl().isBlank()) {
                                dbRestaurant.setCoverPhotoUrl(dbRestaurant.getLogoUrl());
                            }
                            restaurantService.save(dbRestaurant);
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

    @PostMapping("/set-open")
    public String setRestaurantOpen(@RequestParam Long restaurantId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
                    if (restaurantOpt.isPresent() && restaurantOpt.get().getOwner().getId().equals(user.getId())) {
                        Restaurant restaurant = restaurantOpt.get();
                        restaurant.setIsOpen(true);
                        restaurantService.save(restaurant);
                        redirectAttributes.addFlashAttribute("success", "Restaurant set as open.");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "You do not have permission to open this restaurant.");
                    }
                    return "redirect:/restaurant/hours?restaurantId=" + restaurantId;
                }
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/set-closed")
    public String setRestaurantClosed(@RequestParam Long restaurantId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.RESTAURANT_OWNER) {
                    Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
                    if (restaurantOpt.isPresent() && restaurantOpt.get().getOwner().getId().equals(user.getId())) {
                        Restaurant restaurant = restaurantOpt.get();
                        restaurant.setIsOpen(false);
                        restaurantService.save(restaurant);
                        redirectAttributes.addFlashAttribute("success", "Restaurant set as closed.");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "You do not have permission to close this restaurant.");
                    }
                    return "redirect:/restaurant/hours?restaurantId=" + restaurantId;
                }
            }
        }
        return "redirect:/login";
    }
} 