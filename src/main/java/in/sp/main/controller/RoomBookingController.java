package in.sp.main.controller;

import in.sp.main.entity.RoomBooking;
import in.sp.main.entity.Room;
import in.sp.main.entity.Hotel;
import in.sp.main.entity.User;
import in.sp.main.service.RoomBookingService;
import in.sp.main.service.RoomService;
import in.sp.main.service.HotelService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/room-booking")
@RequiredArgsConstructor
public class RoomBookingController {
    
    private final RoomBookingService roomBookingService;
    private final RoomService roomService;
    private final HotelService hotelService;
    private final UserService userService;
    
    @GetMapping("/list")
    public String listBookings(@RequestParam(required = false) Long userId,
                                @RequestParam(required = false) Long roomId,
                                @RequestParam(required = false) Long hotelId,
                                Model model) {
        List<RoomBooking> bookings;
        
        if (userId != null) {
            bookings = roomBookingService.findByUserId(userId);
        } else if (roomId != null) {
            bookings = roomBookingService.findByRoomId(roomId);
        } else if (hotelId != null) {
            bookings = roomBookingService.findByHotelId(hotelId);
        } else {
            bookings = roomBookingService.findAll();
        }
        
        model.addAttribute("bookings", bookings);
        return "room-booking/list";
    }
    
    @GetMapping("/add")
    public String addBookingForm(@RequestParam(required = false) Long roomId,
                                  Model model) {
        RoomBooking booking = new RoomBooking();
        if (roomId != null) {
            Optional<Room> roomOpt = roomService.findById(roomId);
            if (roomOpt.isPresent()) {
                booking.setRoom(roomOpt.get());
            }
        }
        model.addAttribute("booking", booking);
        
        // Get rooms for dropdown
        List<Room> rooms = roomService.findAll();
        model.addAttribute("rooms", rooms);
        
        return "room-booking/add";
    }
    
    @PostMapping("/add")
    public String addBooking(@Valid @ModelAttribute("booking") RoomBooking booking,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            List<Room> rooms = roomService.findAll();
            model.addAttribute("rooms", rooms);
            return "room-booking/add";
        }
        
        try {
            // Set the current user as the booker
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    booking.setUser(userOpt.get());
                    roomBookingService.createBooking(booking);
                    redirectAttributes.addFlashAttribute("success", "Room booking created successfully!");
                    return "redirect:/room-booking/list?userId=" + userOpt.get().getId();
                }
            }
            redirectAttributes.addFlashAttribute("error", "User not found!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
        }
        return "redirect:/room-booking/add";
    }
    
    @GetMapping("/edit/{id}")
    public String editBookingForm(@PathVariable Long id, Model model) {
        Optional<RoomBooking> bookingOpt = roomBookingService.findById(id);
        if (bookingOpt.isPresent()) {
            RoomBooking booking = bookingOpt.get();
            
            // Check if user owns the booking or is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() == User.UserRole.ADMIN || 
                        booking.getUser().getId().equals(user.getId())) {
                        model.addAttribute("booking", booking);
                        List<Room> rooms = roomService.findAll();
                        model.addAttribute("rooms", rooms);
                        return "room-booking/edit";
                    }
                }
            }
        }
        return "redirect:/access-denied";
    }
    
    @PostMapping("/edit/{id}")
    public String editBooking(@PathVariable Long id,
                               @Valid @ModelAttribute("booking") RoomBooking booking,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            List<Room> rooms = roomService.findAll();
            model.addAttribute("rooms", rooms);
            return "room-booking/edit";
        }
        
        try {
            // Check if user owns the booking or is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Optional<RoomBooking> existingOpt = roomBookingService.findById(id);
                    if (existingOpt.isPresent() && 
                        (user.getRole() == User.UserRole.ADMIN || 
                         existingOpt.get().getUser().getId().equals(user.getId()))) {
                        booking.setId(id);
                        roomBookingService.updateBooking(booking);
                        redirectAttributes.addFlashAttribute("success", "Booking updated successfully!");
                        return "redirect:/room-booking/list?userId=" + booking.getUser().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this booking!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
        }
        return "redirect:/room-booking/edit/" + id;
    }
    
    @PostMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<RoomBooking> bookingOpt = roomBookingService.findById(id);
            if (bookingOpt.isPresent()) {
                RoomBooking booking = bookingOpt.get();
                
                // Check if user owns the booking or is admin
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    Optional<User> userOpt = userService.findByEmail(auth.getName());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        if (user.getRole() == User.UserRole.ADMIN || 
                            booking.getUser().getId().equals(user.getId())) {
                            roomBookingService.deleteBooking(id);
                            redirectAttributes.addFlashAttribute("success", "Booking deleted successfully!");
                            return "redirect:/room-booking/list?userId=" + booking.getUser().getId();
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this booking!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting booking: " + e.getMessage());
        }
        return "redirect:/room-booking/list";
    }
    
    @GetMapping("/status/{status}")
    public String bookingsByStatus(@PathVariable RoomBooking.BookingStatus status,
                                   Model model) {
        List<RoomBooking> bookings = roomBookingService.findByStatus(status);
        model.addAttribute("bookings", bookings);
        model.addAttribute("status", status);
        return "room-booking/status";
    }
    
    @GetMapping("/user/{userId}")
    public String bookingsByUser(@PathVariable Long userId, Model model) {
        List<RoomBooking> bookings = roomBookingService.findByUserId(userId);
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
        }
        model.addAttribute("bookings", bookings);
        return "room-booking/user-bookings";
    }
    
    @GetMapping("/room/{roomId}")
    public String bookingsByRoom(@PathVariable Long roomId, Model model) {
        List<RoomBooking> bookings = roomBookingService.findByRoomId(roomId);
        Optional<Room> roomOpt = roomService.findById(roomId);
        if (roomOpt.isPresent()) {
            model.addAttribute("room", roomOpt.get());
        }
        model.addAttribute("bookings", bookings);
        return "room-booking/room-bookings";
    }
    
    @GetMapping("/hotel/{hotelId}")
    public String bookingsByHotel(@PathVariable Long hotelId, Model model) {
        List<RoomBooking> bookings = roomBookingService.findByHotelId(hotelId);
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
        }
        model.addAttribute("bookings", bookings);
        return "room-booking/hotel-bookings";
    }
    
    @GetMapping("/date-range")
    public String bookingsByDateRange(@RequestParam LocalDate startDate,
                                      @RequestParam LocalDate endDate,
                                      Model model) {
        List<RoomBooking> bookings = roomBookingService.findByCheckInDateBetween(startDate, endDate);
        model.addAttribute("bookings", bookings);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "room-booking/date-range";
    }
    
    @GetMapping("/room/{roomId}/availability")
    public String checkRoomAvailability(@PathVariable Long roomId,
                                       @RequestParam LocalDate checkIn,
                                       @RequestParam LocalDate checkOut,
                                       Model model) {
        List<RoomBooking> conflictingBookings = roomBookingService.findByRoomIdAndDateRange(roomId, checkIn, checkOut);
        Optional<Room> roomOpt = roomService.findById(roomId);
        if (roomOpt.isPresent()) {
            model.addAttribute("room", roomOpt.get());
        }
        model.addAttribute("conflictingBookings", conflictingBookings);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("isAvailable", conflictingBookings.isEmpty());
        return "room-booking/availability";
    }
    
    @GetMapping("/details/{id}")
    public String bookingDetails(@PathVariable Long id, Model model) {
        Optional<RoomBooking> bookingOpt = roomBookingService.findById(id);
        if (bookingOpt.isPresent()) {
            model.addAttribute("booking", bookingOpt.get());
            return "room-booking/details";
        }
        return "redirect:/room-booking/list";
    }
} 