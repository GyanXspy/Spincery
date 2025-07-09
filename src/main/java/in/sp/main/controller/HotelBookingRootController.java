package in.sp.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HotelBookingRootController {
    /**
     * Handles the root hotel booking page.
     * Returns the main hotel booking index view.
     */
    @GetMapping("/hotel-booking")
    public String hotelBookingRoot(Model model) {
        return "hotel-booking/index";
    }
} 