package in.sp.main.controller;

import in.sp.main.entity.Hotel;
import in.sp.main.entity.Room;
import in.sp.main.entity.RoomBooking;
import in.sp.main.entity.User;
import in.sp.main.service.HotelService;
import in.sp.main.service.RoomBookingService;
import in.sp.main.service.RoomService;
import in.sp.main.service.UserService;
import in.sp.main.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HotelBookingController {
    
    private final HotelService hotelService;
    private final RoomService roomService;
    private final RoomBookingService roomBookingService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    
    @GetMapping("/booking")
    public String hotelBookingPage(Model model) {
        try {
            List<Hotel> hotels = hotelService.findAll();
            model.addAttribute("hotels", hotels);
        } catch (Exception e) {
            model.addAttribute("hotels", new ArrayList<>());
            model.addAttribute("error", "Error loading hotels: " + e.getMessage());
        }
        return "hotel-booking/index";
    }
    
    @GetMapping("/hotels")
    public String hotelsPage(Model model) {
        try {
            List<Hotel> hotels = hotelService.findAll();
            model.addAttribute("hotels", hotels);
        } catch (Exception e) {
            model.addAttribute("hotels", new ArrayList<>());
            model.addAttribute("error", "Error loading hotels: " + e.getMessage());
        }
        return "hotel-booking/hotels";
    }
    
    @GetMapping("/hotel-details/{id}")
    public String hotelDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/hotel/hotels";
        }
        
        Optional<Hotel> hotelOpt = hotelService.findById(id);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            try {
                List<Room> rooms = roomService.findByHotelId(id);
                model.addAttribute("hotel", hotel);
                model.addAttribute("rooms", rooms);
            } catch (Exception e) {
                model.addAttribute("hotel", hotel);
                model.addAttribute("rooms", new ArrayList<>());
                model.addAttribute("error", "Error loading rooms: " + e.getMessage());
            }
            return "hotel-booking/hotel-details";
        }
        return "redirect:/hotel/hotels";
    }
    
    @GetMapping("/hotel/{hotelId}/rooms")
    public String hotelRooms(@PathVariable Long hotelId, Model model) {
        if (hotelId == null) {
            return "redirect:/hotel/hotels";
        }
        
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        List<Room> rooms = new ArrayList<>();
        
        try {
            rooms = roomService.findByHotelId(hotelId);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading rooms: " + e.getMessage());
        }
        
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            model.addAttribute("hotel", hotel);
            model.addAttribute("rooms", rooms);
            return "hotel-booking/rooms";
        }
        return "redirect:/hotel/hotels";
    }
    
    @GetMapping("/room/{roomId}/book")
    public String bookRoom(@PathVariable Long roomId, Model model) {
        if (roomId == null) {
            return "redirect:/hotel/hotels";
        }
        
        Optional<Room> roomOpt = roomService.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            model.addAttribute("room", room);
            model.addAttribute("booking", new RoomBooking());
            return "hotel-booking/book-room";
        }
        return "redirect:/hotel/hotels";
    }
    
    @PostMapping("/booking/create")
    public String createBooking(@ModelAttribute RoomBooking booking, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                booking.setUser(user);
                try {
                    RoomBooking savedBooking = roomBookingService.createBooking(booking);
                    model.addAttribute("booking", savedBooking);
                    return "hotel-booking/booking-confirmation";
                } catch (Exception e) {
                    model.addAttribute("error", "Error creating booking: " + e.getMessage());
                    return "hotel-booking/book-room";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/bookings")
    public String userBookings(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                try {
                    List<RoomBooking> bookings = roomBookingService.findByUserId(user.getId());
                    model.addAttribute("bookings", bookings);
                } catch (Exception e) {
                    model.addAttribute("bookings", new ArrayList<>());
                    model.addAttribute("error", "Error loading bookings: " + e.getMessage());
                }
                return "hotel-booking/bookings";
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/booking/{id}")
    public String bookingDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/hotel/bookings";
        }
        
        Optional<RoomBooking> bookingOpt = roomBookingService.findById(id);
        if (bookingOpt.isPresent()) {
            RoomBooking booking = bookingOpt.get();
            model.addAttribute("booking", booking);
            return "hotel-booking/booking-details";
        }
        return "redirect:/hotel/bookings";
    }
    
    @GetMapping("/search-hotels")
    public String searchHotels(@RequestParam String city, 
                              @RequestParam(required = false) LocalDate checkIn, 
                              @RequestParam(required = false) LocalDate checkOut, 
                              Model model) {
        try {
            List<Hotel> hotels = hotelService.findByCity(city);
            model.addAttribute("hotels", hotels);
        } catch (Exception e) {
            model.addAttribute("hotels", new ArrayList<>());
            model.addAttribute("error", "Error searching hotels: " + e.getMessage());
        }
        model.addAttribute("city", city);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        return "hotel-booking/search-results";
    }

    @GetMapping("/dashboard")
    public String hotelDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.HOTEL_OWNER) {
                    model.addAttribute("user", user);
                    model.addAttribute("userInitial", user.getName().substring(0, 1).toUpperCase());
                    return "hotel/dashboard";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/register")
    public String hotelRegister(Model model) {
        if (!model.containsAttribute("hotel")) {
            model.addAttribute("hotel", new Hotel());
        }
        return "hotel/register";
    }

    @PostMapping("/register")
    public String registerHotel(@ModelAttribute Hotel hotel,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                hotel.setOwner(user);
                hotel.setOwnerName(user.getName());
                hotel.setEmail(user.getEmail());
                hotel.setPhone(user.getPhone());
            } else {
                model.addAttribute("error", "Could not determine owner for this hotel.");
                return "hotel/register";
            }
            String imageUrl = cloudinaryService.uploadFile(imageFile, "hotel");
            hotel.setHotelLogoUrl(imageUrl);
            hotelService.save(hotel);
            model.addAttribute("success", "Hotel registered successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error registering Hotel: " + e.getMessage());
        }
        return "hotel/register";
    }
    
    @GetMapping("/list")
    public String hotelList(@RequestParam(required = false) String city, Model model) {
        List<Hotel> hotels;
        try {
            if (city != null && !city.trim().isEmpty()) {
                hotels = hotelService.findByCity(city);
            } else {
                hotels = hotelService.findAll();
            }
        } catch (Exception e) {
            hotels = new ArrayList<>();
            model.addAttribute("error", "Error loading hotels: " + e.getMessage());
        }
        model.addAttribute("hotels", hotels);
        model.addAttribute("city", city != null ? city : "");
        return "hotel/list";
    }
    
    @GetMapping("/details")
    public String hotelDetailsById(@RequestParam Long hotelId, Model model) {
        if (hotelId == null) {
            return "redirect:/hotel/list";
        }
        
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            model.addAttribute("hotel", hotel);
            return "hotel/details";
        } else {
            model.addAttribute("error", "Hotel not found");
            return "redirect:/hotel/list";
        }
    }
    
    @GetMapping("/analytics")
    public String hotelAnalytics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.HOTEL_OWNER) {
                    model.addAttribute("user", user);
                    // Add analytics data here
                    return "hotel/analytics";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
} 