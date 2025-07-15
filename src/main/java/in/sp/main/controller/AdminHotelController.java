package in.sp.main.controller;

import in.sp.main.entity.Hotel;
import in.sp.main.service.HotelService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
public class AdminHotelController {
    private final HotelService hotelService;
    private final UserService userService;

    @GetMapping("")
    public String hotelsPage(Model model) {
        List<Hotel> allHotels = hotelService.findAll();
        List<Hotel> pendingHotels = hotelService.findByIsVerified(false);
        List<Hotel> verifiedHotels = hotelService.findByIsVerified(true);
        model.addAttribute("totalHotels", allHotels.size());
        model.addAttribute("pendingCount", pendingHotels.size());
        model.addAttribute("verifiedCount", verifiedHotels.size());
        model.addAttribute("pendingHotels", pendingHotels);
        model.addAttribute("verifiedHotels", verifiedHotels);
        return "admin/hotels";
    }

    @PostMapping("/verify")
    public String verifyHotel(@RequestParam Long hotelId) {
        hotelService.verifyHotel(hotelId);
        return "redirect:/admin/hotels";
    }

    @PostMapping("/reject")
    public String rejectHotel(@RequestParam Long hotelId) {
        hotelService.rejectHotel(hotelId);
        return "redirect:/admin/hotels";
    }
} 