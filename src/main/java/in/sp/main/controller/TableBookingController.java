package in.sp.main.controller;

import in.sp.main.entity.Restaurant;
import in.sp.main.entity.TableBooking;
import in.sp.main.entity.User;
import in.sp.main.service.CloudinaryService;
import in.sp.main.service.RestaurantService;
import in.sp.main.service.TableBookingService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/table-booking")
@RequiredArgsConstructor
public class TableBookingController {
    
    private final RestaurantService restaurantService;
    private final TableBookingService tableBookingService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    
    /**
     * Displays the main table booking page with a list of all restaurants.
     * Handles errors in loading restaurants gracefully.
     */
    @GetMapping("")
    public String tableBookingHome(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findAll();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
        }
        return "table-booking/index";
    }
    
    /**
     * Displays the list of restaurants available for table booking.
     * Handles errors in loading restaurants gracefully.
     */
    @GetMapping("/restaurants/table-booking")
    public String restaurantsForTableBooking(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findAll();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
        }
        return "table-booking/restaurants";
    }
    
    /**
     * Displays the table booking form for a specific restaurant by its ID.
     * Adds the restaurant and a new booking object to the model.
     */
    @GetMapping("/restaurant/{restaurantId}/table-booking")
    public String restaurantTableBooking(@PathVariable Long restaurantId, Model model) {
        if (restaurantId == null) {
            return "redirect:/restaurants/table-booking";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("booking", new TableBooking());
            return "table-booking/book-table";
        }
        return "redirect:/restaurants/table-booking";
    }
    
    /**
     * Handles table booking creation.
     * Associates the booking with the authenticated user and saves it.
     */
    @PostMapping("/create")
    public String createTableBooking(@ModelAttribute TableBooking booking, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                booking.setUser(user);
                try {
                    TableBooking savedBooking = tableBookingService.createBooking(booking);
                    model.addAttribute("booking", savedBooking);
                    return "table-booking/booking-confirmation";
                } catch (Exception e) {
                    model.addAttribute("error", "Error creating booking: " + e.getMessage());
                    return "table-booking/book-table";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Handles table booking registration with image upload.
     * Uploads the image and saves the booking with the image URL.
     */
    @PostMapping("/register")
    public String registerTableBooking(@ModelAttribute TableBooking tableBooking,
                                       @RequestParam("imageFile") MultipartFile imageFile,
                                       Model model) {
        try {
            String imageUrl = cloudinaryService.uploadFile(imageFile, "tablebooking");
            tableBooking.setImageUrl(imageUrl);
            tableBookingService.createBooking(tableBooking);
            model.addAttribute("success", "Table Booking registered successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error registering Table Booking: " + e.getMessage());
        }
        return "table-booking/book-table";
    }
    
    /**
     * Displays the list of table bookings for the authenticated user.
     * Loads all table bookings placed by the user.
     */
    @GetMapping("/bookings")
    public String userTableBookings(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                try {
                    List<TableBooking> bookings = tableBookingService.findByUserId(user.getId());
                    model.addAttribute("bookings", bookings);
                } catch (Exception e) {
                    model.addAttribute("bookings", new ArrayList<>());
                    model.addAttribute("error", "Error loading bookings: " + e.getMessage());
                }
                return "table-booking/bookings";
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Displays the details of a specific table booking by its ID.
     * Shows booking details or redirects if not found.
     */
    @GetMapping("/{id}")
    public String tableBookingDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/bookings";
        }
        
        Optional<TableBooking> bookingOpt = tableBookingService.findById(id);
        if (bookingOpt.isPresent()) {
            TableBooking booking = bookingOpt.get();
            model.addAttribute("booking", booking);
            return "table-booking/booking-details";
        }
        return "redirect:/bookings";
    }
    
    /**
     * Checks the availability of tables for a specific restaurant on given date and time.
     * Adds the restaurant, date, time, guest count, and availability status to the model.
     */
    @GetMapping("/restaurant/{restaurantId}/available-tables")
    public String checkAvailableTables(@PathVariable Long restaurantId, 
                                     @RequestParam LocalDate date,
                                     @RequestParam LocalTime time,
                                     @RequestParam Integer guests,
                                     Model model) {
        if (restaurantId == null || date == null || time == null || guests == null) {
            return "redirect:/restaurants/table-booking";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            try {
                boolean hasAvailableTables = tableBookingService.checkAvailability(restaurantId, date, time, guests);
                model.addAttribute("restaurant", restaurant);
                model.addAttribute("date", date);
                model.addAttribute("time", time);
                model.addAttribute("guests", guests);
                model.addAttribute("hasAvailableTables", hasAvailableTables);
            } catch (Exception e) {
                model.addAttribute("restaurant", restaurant);
                model.addAttribute("date", date);
                model.addAttribute("time", time);
                model.addAttribute("guests", guests);
                model.addAttribute("hasAvailableTables", false);
                model.addAttribute("error", "Error checking availability: " + e.getMessage());
            }
            return "table-booking/availability-check";
        }
        return "redirect:/restaurants/table-booking";
    }
    
    /**
     * Searches for restaurants by city and optional booking parameters.
     * Adds the search criteria and results to the model.
     */
    @GetMapping("/search-restaurants-table-booking")
    public String searchRestaurantsForTableBooking(@RequestParam String city, 
                                                 @RequestParam(required = false) LocalDate date,
                                                 @RequestParam(required = false) LocalTime time,
                                                 @RequestParam(required = false) Integer guests,
                                                 Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findByCity(city);
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error searching restaurants: " + e.getMessage());
        }
        model.addAttribute("city", city);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("guests", guests);
        return "table-booking/search-results";
    }
} 