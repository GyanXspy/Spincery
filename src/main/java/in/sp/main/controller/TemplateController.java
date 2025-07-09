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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TemplateController {
    
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final HotelService hotelService;
    private final FoodOrderService foodOrderService;
    private final RoomBookingService roomBookingService;
    private final MenuItemService menuItemService;
    
    /**
     * Displays the access denied page.
     * Returns the access denied view for unauthorized users.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
    
    /**
     * Displays the error page.
     * Returns the error view for general errors.
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
    
    /**
     * Displays the main dashboard with user information.
     * Loads authenticated user details and adds them to the model.
     */
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
    
    /**
     * Displays the food delivery index page.
     * Loads user information for the food delivery interface.
     */
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
    
    /**
     * Displays the list of restaurants for food delivery.
     * Loads verified restaurants and adds them to the model.
     */
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
    
    /**
     * Displays the details of a specific restaurant for food delivery.
     * Loads restaurant details and menu items for the food delivery interface.
     */
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
    
    /**
     * Displays the menu for food delivery, optionally for a specific restaurant.
     * Loads menu items and restaurant information for the food delivery interface.
     */
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
    
    /**
     * Displays the list of food orders for the authenticated user.
     * Loads user's food orders for the food delivery interface.
     */
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
    
    /**
     * Displays the details of a specific food order for food delivery.
     * Loads order details and order items for the food delivery interface.
     */
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
    
    /**
     * Displays the food delivery order confirmation page.
     * Returns the order confirmation view for food delivery.
     */
    @GetMapping("/food-delivery/order-confirmation")
    public String foodDeliveryOrderConfirmation(Model model) {
        return "food-delivery/order-confirmation";
    }
    
    /**
     * Displays the food delivery order tracking page.
     * Returns the order tracking view for food delivery.
     */
    @GetMapping("/food-delivery/track-order")
    public String foodDeliveryTrackOrder(Model model) {
        return "food-delivery/track-order";
    }
    
    /**
     * Displays the food delivery order tracking details.
     * Loads order tracking information for the food delivery interface.
     */
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
    
    /**
     * Displays the hotel booking index page.
     * Returns the main hotel booking view.
     */
    @GetMapping("/hotel-booking/index")
    public String hotelBookingIndex(Model model) {
        return "hotel-booking/index";
    }
    
    /**
     * Displays the list of hotels for hotel booking.
     * Loads all hotels and adds them to the model.
     */
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
    
    /**
     * Displays the details of a specific hotel for hotel booking.
     * Loads hotel details and room information for the hotel booking interface.
     */
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
    
    /**
     * Displays the room booking form for hotel booking.
     * Returns the room booking view for hotel booking.
     */
    @GetMapping("/hotel-booking/book-room")
    public String hotelBookingBookRoom(@RequestParam(required = false) Long roomId, Model model) {
        // Note: Room functionality would need to be implemented
        // For now, we'll just show the booking form
        return "hotel-booking/book-room";
    }
    
    /**
     * Displays the list of hotel bookings for the authenticated user.
     * Loads user's hotel bookings for the hotel booking interface.
     */
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