package in.sp.main.controller;

import in.sp.main.entity.Room;
import in.sp.main.entity.Hotel;
import in.sp.main.entity.User;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    private final HotelService hotelService;
    private final UserService userService;
    
    /**
     * Displays a list of rooms, optionally filtered by hotel.
     * Adds the rooms and hotel info to the model.
     */
    @GetMapping("/list")
    public String listRooms(@RequestParam(required = false) Long hotelId,
                             Model model) {
        List<Room> rooms;
        if (hotelId != null) {
            rooms = roomService.findByHotelId(hotelId);
            Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
            if (hotelOpt.isPresent()) {
                model.addAttribute("hotel", hotelOpt.get());
            }
        } else {
            rooms = roomService.findAll();
        }
        model.addAttribute("rooms", rooms);
        return "room/list";
    }
    
    /**
     * Displays the form to add a new room, optionally for a specific hotel.
     * Adds available hotels to the model for selection.
     */
    @GetMapping("/add")
    public String addRoomForm(@RequestParam(required = false) Long hotelId,
                               Model model) {
        Room room = new Room();
        if (hotelId != null) {
            Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
            if (hotelOpt.isPresent()) {
                room.setHotel(hotelOpt.get());
            }
        }
        model.addAttribute("room", room);
        
        // Get hotels for dropdown
        List<Hotel> hotels = hotelService.findAll();
        model.addAttribute("hotels", hotels);
        
        return "room/add";
    }
    
    /**
     * Handles the submission of the add room form.
     * Validates input, checks ownership, and saves the room if authorized.
     */
    @PostMapping("/add")
    public String addRoom(@Valid @ModelAttribute("room") Room room,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            List<Hotel> hotels = hotelService.findAll();
            model.addAttribute("hotels", hotels);
            return "room/add";
        }
        
        try {
            // Check if user is hotel owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.HOTEL_OWNER) {
                    // Verify hotel ownership
                    if (room.getHotel() != null && 
                        room.getHotel().getOwner().getId().equals(userOpt.get().getId())) {
                        roomService.save(room);
                        redirectAttributes.addFlashAttribute("success", "Room added successfully!");
                        return "redirect:/room/list?hotelId=" + room.getHotel().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to add rooms to this hotel!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding room: " + e.getMessage());
        }
        return "redirect:/room/add";
    }
    
    /**
     * Displays the form to edit an existing room by its ID.
     * Checks ownership and loads the room and hotels for editing.
     */
    @GetMapping("/edit/{id}")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Optional<Room> roomOpt = roomService.findById(id);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            // Check if user is hotel owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.HOTEL_OWNER) {
                    if (room.getHotel() != null && 
                        room.getHotel().getOwner().getId().equals(userOpt.get().getId())) {
                        model.addAttribute("room", room);
                        List<Hotel> hotels = hotelService.findAll();
                        model.addAttribute("hotels", hotels);
                        return "room/edit";
                    }
                }
            }
        }
        return "redirect:/access-denied";
    }
    
    /**
     * Handles the submission of the edit room form.
     * Validates input, checks ownership, and updates the room if authorized.
     */
    @PostMapping("/edit/{id}")
    public String editRoom(@PathVariable Long id,
                            @Valid @ModelAttribute("room") Room room,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            List<Hotel> hotels = hotelService.findAll();
            model.addAttribute("hotels", hotels);
            return "room/edit";
        }
        
        try {
            // Check if user is hotel owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.HOTEL_OWNER) {
                    if (room.getHotel() != null && 
                        room.getHotel().getOwner().getId().equals(userOpt.get().getId())) {
                        room.setId(id);
                        roomService.save(room);
                        redirectAttributes.addFlashAttribute("success", "Room updated successfully!");
                        return "redirect:/room/list?hotelId=" + room.getHotel().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this room!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating room: " + e.getMessage());
        }
        return "redirect:/room/edit/" + id;
    }
    
    /**
     * Deletes a room by its ID if the user is authorized.
     * Checks ownership and removes the room if permitted.
     */
    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Room> roomOpt = roomService.findById(id);
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                
                // Check if user is hotel owner
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    Optional<User> userOpt = userService.findByEmail(auth.getName());
                    if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.HOTEL_OWNER) {
                        if (room.getHotel() != null && 
                            room.getHotel().getOwner().getId().equals(userOpt.get().getId())) {
                            roomService.deleteById(id);
                            redirectAttributes.addFlashAttribute("success", "Room deleted successfully!");
                            return "redirect:/room/list?hotelId=" + room.getHotel().getId();
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this room!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting room: " + e.getMessage());
        }
        return "redirect:/room/list";
    }
    
    /**
     * Displays available rooms for a specific hotel by hotel ID.
     * Adds the hotel and available rooms to the model.
     */
    @GetMapping("/available/{hotelId}")
    public String availableRooms(@PathVariable Long hotelId, Model model) {
        List<Room> rooms = roomService.findByHotelIdAndIsAvailableTrue(hotelId);
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
        }
        model.addAttribute("rooms", rooms);
        return "room/available";
    }
    
    /**
     * Displays rooms filtered by room type.
     * Adds the room type and rooms to the model.
     */
    @GetMapping("/type/{roomType}")
    public String roomsByType(@PathVariable String roomType, Model model) {
        List<Room> rooms = roomService.findByRoomType(roomType);
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomType", roomType);
        return "room/type";
    }
    
    /**
     * Displays rooms for a specific hotel and room type.
     * Adds the hotel, room type, and rooms to the model.
     */
    @GetMapping("/hotel/{hotelId}/type/{roomType}")
    public String roomsByHotelAndType(@PathVariable Long hotelId,
                                      @PathVariable String roomType,
                                      Model model) {
        List<Room> rooms = roomService.findByHotelIdAndRoomType(hotelId, roomType);
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomType", roomType);
        return "room/hotel-type";
    }
    
    /**
     * Displays rooms for a specific hotel within a price range.
     * Adds the hotel, price range, and rooms to the model.
     */
    @GetMapping("/price-range")
    public String roomsByPriceRange(@RequestParam Long hotelId,
                                    @RequestParam Double minPrice,
                                    @RequestParam Double maxPrice,
                                    Model model) {
        List<Room> rooms = roomService.findByHotelIdAndPriceRange(hotelId, minPrice, maxPrice);
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "room/price-range";
    }
    
    /**
     * Displays rooms for a specific hotel with minimum capacity.
     * Adds the hotel, capacity, and rooms to the model.
     */
    @GetMapping("/capacity/{hotelId}/{capacity}")
    public String roomsByCapacity(@PathVariable Long hotelId,
                                  @PathVariable Integer capacity,
                                  Model model) {
        List<Room> rooms = roomService.findByHotelIdAndCapacityGreaterThanEqual(hotelId, capacity);
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("capacity", capacity);
        return "room/capacity";
    }
    
    /**
     * Displays available rooms for a specific hotel within a maximum price.
     * Adds the hotel, max price, and rooms to the model.
     */
    @GetMapping("/available-price/{hotelId}/{maxPrice}")
    public String availableRoomsByPrice(@PathVariable Long hotelId,
                                        @PathVariable Double maxPrice,
                                        Model model) {
        List<Room> rooms = roomService.findByHotelIdAndAvailableAndPriceLessThanEqual(hotelId, maxPrice);
        Optional<Hotel> hotelOpt = hotelService.findById(hotelId);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("maxPrice", maxPrice);
        return "room/available-price";
    }
} 