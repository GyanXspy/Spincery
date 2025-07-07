package in.sp.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HotelBookingRootController {
    @GetMapping("/hotel-booking")
    public String hotelBookingRoot(Model model) {
        return "hotel-booking/index";
    }
} 