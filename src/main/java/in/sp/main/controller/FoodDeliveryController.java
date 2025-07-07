package in.sp.main.controller;

import in.sp.main.entity.FoodOrder;
import in.sp.main.entity.MenuItem;
import in.sp.main.entity.Restaurant;
import in.sp.main.entity.User;
import in.sp.main.service.FoodOrderService;
import in.sp.main.service.MenuItemService;
import in.sp.main.service.RestaurantService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class FoodDeliveryController {
    
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final FoodOrderService foodOrderService;
    private final UserService userService;
    
    @GetMapping("/food-delivery")
    public String foodDeliveryPage(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findAll();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
        }
        return "food-delivery/index";
    }
    
    @GetMapping("/restaurants")
    public String restaurantsPage(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findAll();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
        }
        return "food-delivery/restaurants";
    }
    
    @GetMapping("/restaurant-details/{id}")
    public String restaurantDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/restaurants";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(id);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            try {
                List<MenuItem> menuItems = menuItemService.findByRestaurantId(id);
                model.addAttribute("restaurant", restaurant);
                model.addAttribute("menuItems", menuItems);
            } catch (Exception e) {
                model.addAttribute("restaurant", restaurant);
                model.addAttribute("menuItems", new ArrayList<>());
                model.addAttribute("error", "Error loading menu items: " + e.getMessage());
            }
            return "food-delivery/restaurant-details";
        }
        return "redirect:/restaurants";
    }
    
    @GetMapping("/restaurant/{restaurantId}/menu")
    public String restaurantMenu(@PathVariable Long restaurantId, Model model) {
        if (restaurantId == null) {
            return "redirect:/restaurants";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
        List<MenuItem> menuItems = new ArrayList<>();
        
        try {
            menuItems = menuItemService.findByRestaurantId(restaurantId);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading menu items: " + e.getMessage());
        }
        
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("menuItems", menuItems);
            return "food-delivery/menu";
        }
        return "redirect:/restaurants";
    }
    
    @PostMapping("/order/create")
    public String createOrder(@ModelAttribute FoodOrder order, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                order.setUser(user);
                try {
                    FoodOrder savedOrder = foodOrderService.createOrder(order);
                    model.addAttribute("order", savedOrder);
                    return "food-delivery/order-confirmation";
                } catch (Exception e) {
                    model.addAttribute("error", "Error creating order: " + e.getMessage());
                    return "food-delivery/menu";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/orders")
    public String userOrders(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                try {
                    List<FoodOrder> orders = foodOrderService.findByUserId(user.getId());
                    model.addAttribute("orders", orders);
                } catch (Exception e) {
                    model.addAttribute("orders", new ArrayList<>());
                    model.addAttribute("error", "Error loading orders: " + e.getMessage());
                }
                return "food-delivery/orders";
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/order-details/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/orders";
        }
        
        Optional<FoodOrder> orderOpt = foodOrderService.findById(id);
        if (orderOpt.isPresent()) {
            FoodOrder order = orderOpt.get();
            model.addAttribute("order", order);
            return "food-delivery/order-details";
        }
        return "redirect:/orders";
    }
    
    @GetMapping("/track-order")
    public String trackOrderPage() {
        return "food-delivery/track-order";
    }
    
    @GetMapping("/track-order/{orderId}")
    public String trackOrder(@PathVariable Long orderId, Model model) {
        if (orderId == null) {
            return "redirect:/track-order";
        }
        
        Optional<FoodOrder> orderOpt = foodOrderService.findById(orderId);
        if (orderOpt.isPresent()) {
            FoodOrder order = orderOpt.get();
            model.addAttribute("order", order);
            return "food-delivery/tracking";
        }
        return "redirect:/track-order";
    }
} 