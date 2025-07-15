package in.sp.main.controller;

import in.sp.main.entity.CloudKitchen;
import in.sp.main.service.CloudKitchenService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/cloud-kitchens")
@RequiredArgsConstructor
public class AdminCloudKitchenController {
    private final CloudKitchenService cloudKitchenService;
    private final UserService userService;

    @GetMapping("")
    public String cloudKitchensPage(Model model) {
        List<CloudKitchen> allKitchens = cloudKitchenService.findAll();
        List<CloudKitchen> pendingKitchens = cloudKitchenService.findByIsVerified(false);
        List<CloudKitchen> verifiedKitchens = cloudKitchenService.findByIsVerified(true);
        model.addAttribute("totalCloudKitchens", allKitchens.size());
        model.addAttribute("pendingCount", pendingKitchens.size());
        model.addAttribute("verifiedCount", verifiedKitchens.size());
        model.addAttribute("pendingCloudKitchens", pendingKitchens);
        model.addAttribute("verifiedCloudKitchens", verifiedKitchens);
        return "admin/cloud-kitchens";
    }

    @PostMapping("/verify")
    public String verifyCloudKitchen(@RequestParam Long kitchenId) {
        cloudKitchenService.verifyCloudKitchen(kitchenId);
        return "redirect:/admin/cloud-kitchens";
    }

    @PostMapping("/reject")
    public String rejectCloudKitchen(@RequestParam Long kitchenId) {
        cloudKitchenService.rejectCloudKitchen(kitchenId);
        return "redirect:/admin/cloud-kitchens";
    }
} 