package in.sp.main.controller;

import in.sp.main.entity.*;
import in.sp.main.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TemplateController {
    
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final HotelService hotelService;
    private final CloudKitchenService cloudKitchenService;
    private final FoodOrderService foodOrderService;
    private final RoomBookingService roomBookingService;
    private final TableBookingService tableBookingService;
    private final MenuItemService menuItemService;
    private final MealPlanService mealPlanService;
    private final CloudKitchenSubscriptionService subscriptionService;
    
    // Error and Access Pages
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
    
    @GetMapping("/error")
    public String error() {
        return "error";
    }
    
    // Main Dashboard
    @GetMapping("/main-dashboard")
    public String mainDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                var userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    model.addAttribute("user", user);
                    model.addAttribute("userInitial", user.getName().substring(0, 1).toUpperCase());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading user information: " + e.getMessage());
            }
        }
        return "dashboard";
    }
    
    // Food Delivery Templates - Only missing endpoints
    @GetMapping("/food-delivery/index")
    public String foodDeliveryIndex(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                var userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    model.addAttribute("user", userOpt.get());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading user information: " + e.getMessage());
            }
        }
        return "food-delivery/index";
    }
    
    @GetMapping("/food-delivery/restaurants")
    public String foodDeliveryRestaurants(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findByIsVerifiedTrue();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
            model.addAttribute("restaurants", new ArrayList<>());
        }
        return "food-delivery/restaurants";
    }
    
    @GetMapping("/food-delivery/restaurant-details/{id}")
    public String foodDeliveryRestaurantDetails(@PathVariable Long id, Model model) {
        try {
            var restaurantOpt = restaurantService.findById(id);
            if (restaurantOpt.isPresent()) {
                Restaurant restaurant = restaurantOpt.get();
                model.addAttribute("restaurant", restaurant);
                
                // Get menu items
                List<MenuItem> menuItems = menuItemService.findByRestaurantId(id);
                model.addAttribute("menuItems", menuItems);
            } else {
                model.addAttribute("error", "Restaurant not found");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading restaurant details: " + e.getMessage());
        }
        return "food-delivery/restaurant-details";
    }
    
    @GetMapping("/food-delivery/menu")
    public String foodDeliveryMenu(@RequestParam(required = false) Long restaurantId, Model model) {
        try {
            if (restaurantId != null) {
                var restaurantOpt = restaurantService.findById(restaurantId);
                if (restaurantOpt.isPresent()) {
                    model.addAttribute("restaurant", restaurantOpt.get());
                    List<MenuItem> menuItems = menuItemService.findByRestaurantId(restaurantId);
                    model.addAttribute("menuItems", menuItems);
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading menu: " + e.getMessage());
        }
        return "food-delivery/menu";
    }
    
    @GetMapping("/food-delivery/orders")
    public String foodDeliveryOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                var userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    List<FoodOrder> orders = foodOrderService.findByUserId(userOpt.get().getId());
                    model.addAttribute("orders", orders);
                    model.addAttribute("user", userOpt.get());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading orders: " + e.getMessage());
                model.addAttribute("orders", new ArrayList<>());
            }
        }
        return "food-delivery/orders";
    }
    
    @GetMapping("/food-delivery/order-details/{id}")
    public String foodDeliveryOrderDetails(@PathVariable Long id, Model model) {
        try {
            var orderOpt = foodOrderService.findById(id);
            if (orderOpt.isPresent()) {
                FoodOrder order = orderOpt.get();
                model.addAttribute("order", order);
                
                // Note: Order items functionality would need to be implemented in FoodOrderService
                // For now, we'll just show the order without items
                model.addAttribute("orderItems", new ArrayList<>());
            } else {
                model.addAttribute("error", "Order not found");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading order details: " + e.getMessage());
        }
        return "food-delivery/order-details";
    }
    
    @GetMapping("/food-delivery/order-confirmation")
    public String foodDeliveryOrderConfirmation(Model model) {
        return "food-delivery/order-confirmation";
    }
    
    @GetMapping("/food-delivery/track-order")
    public String foodDeliveryTrackOrder(Model model) {
        return "food-delivery/track-order";
    }
    
    @GetMapping("/food-delivery/tracking")
    public String foodDeliveryTracking(@RequestParam(required = false) Long orderId, Model model) {
        if (orderId != null) {
            try {
                var orderOpt = foodOrderService.findById(orderId);
                if (orderOpt.isPresent()) {
                    model.addAttribute("order", orderOpt.get());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading order: " + e.getMessage());
            }
        }
        return "food-delivery/tracking";
    }
    
    // Hotel Booking Templates - Only missing endpoints
    @GetMapping("/hotel-booking/index")
    public String hotelBookingIndex(Model model) {
        return "hotel-booking/index";
    }
    
    @GetMapping("/hotel-booking/hotels")
    public String hotelBookingHotels(Model model) {
        try {
            List<Hotel> hotels = hotelService.findAll();
            model.addAttribute("hotels", hotels);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading hotels: " + e.getMessage());
            model.addAttribute("hotels", new ArrayList<>());
        }
        return "hotel-booking/hotels";
    }
    
    @GetMapping("/hotel-booking/hotel-details/{id}")
    public String hotelBookingHotelDetails(@PathVariable Long id, Model model) {
        try {
            var hotelOpt = hotelService.findById(id);
            if (hotelOpt.isPresent()) {
                Hotel hotel = hotelOpt.get();
                model.addAttribute("hotel", hotel);
                
                // Note: Room functionality would need to be implemented in HotelService
                // For now, we'll just show the hotel without rooms
                model.addAttribute("rooms", new ArrayList<>());
            } else {
                model.addAttribute("error", "Hotel not found");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading hotel details: " + e.getMessage());
        }
        return "hotel-booking/hotel-details";
    }
    
    @GetMapping("/hotel-booking/book-room")
    public String hotelBookingBookRoom(@RequestParam(required = false) Long roomId, Model model) {
        // Note: Room functionality would need to be implemented
        // For now, we'll just show the booking form
        return "hotel-booking/book-room";
    }
    
    @GetMapping("/hotel-booking/bookings")
    public String hotelBookingBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                var userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    List<RoomBooking> bookings = roomBookingService.findByUserId(userOpt.get().getId());
                    model.addAttribute("bookings", bookings);
                    model.addAttribute("user", userOpt.get());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading bookings: " + e.getMessage());
                model.addAttribute("bookings", new ArrayList<>());
            }
        }
        return "hotel-booking/bookings";
    }
    
    @GetMapping("/hotel-booking/booking-details/{id}")
    public String hotelBookingBookingDetails(@PathVariable Long id, Model model) {
        try {
            var bookingOpt = roomBookingService.findById(id);
            if (bookingOpt.isPresent()) {
                RoomBooking booking = bookingOpt.get();
                model.addAttribute("booking", booking);
            } else {
                model.addAttribute("error", "Booking not found");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading booking details: " + e.getMessage());
        }
        return "hotel-booking/booking-details";
    }
    
    @GetMapping("/hotel-booking/booking-confirmation")
    public String hotelBookingConfirmation(Model model) {
        return "hotel-booking/booking-confirmation";
    }
    
    // Cloud Kitchen Templates - Only missing endpoints (removing conflicting ones)

    

    

    

    

    

    

    

    
    // Table Booking Templates - Only missing endpoints
    @GetMapping("/table-booking/index")
    public String tableBookingIndex(Model model) {
        return "table-booking/index";
    }
    
    @GetMapping("/table-booking/restaurants")
    public String tableBookingRestaurants(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findByIsVerifiedTrue();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
            model.addAttribute("restaurants", new ArrayList<>());
        }
        return "table-booking/restaurants";
    }
    
    @GetMapping("/table-booking/book-table")
    public String tableBookingBookTable(@RequestParam(required = false) Long restaurantId, Model model) {
        if (restaurantId != null) {
            try {
                var restaurantOpt = restaurantService.findById(restaurantId);
                if (restaurantOpt.isPresent()) {
                    model.addAttribute("restaurant", restaurantOpt.get());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error loading restaurant: " + e.getMessage());
            }
        }
        return "table-booking/book-table";
    }
    
    @GetMapping("/table-booking/availability-check")
    public String tableBookingAvailabilityCheck(Model model) {
        return "table-booking/availability-check";
    }
    
    @GetMapping("/table-booking/booking-confirmation")
    public String tableBookingConfirmation(Model model) {
        return "table-booking/booking-confirmation";
    }
    
    @GetMapping("/table-booking/reservations")
    public String tableBookingReservations(Model model) {
        return "table-booking/reservations";
    }
    
    @GetMapping("/table-booking/search-results")
    public String tableBookingSearchResults(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            try {
                List<Restaurant> restaurants = restaurantService.findByIsVerifiedTrue().stream()
                    .filter(r -> r.getRestaurantName().toLowerCase().contains(query.toLowerCase()) ||
                                r.getCity().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
                model.addAttribute("restaurants", restaurants);
                model.addAttribute("searchQuery", query);
            } catch (Exception e) {
                model.addAttribute("error", "Error searching restaurants: " + e.getMessage());
                model.addAttribute("restaurants", new ArrayList<>());
            }
        }
        return "table-booking/search-results";
    }
    
    // Hotel Templates - Only missing endpoints

    

    

} 