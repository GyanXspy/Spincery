package in.sp.main.controller;

import in.sp.main.entity.MealPlan;
import in.sp.main.entity.CloudKitchen;
import in.sp.main.entity.User;
import in.sp.main.service.MealPlanService;
import in.sp.main.service.CloudKitchenService;
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
@RequestMapping("/meal-plan")
@RequiredArgsConstructor
public class MealPlanController {
    
    private final MealPlanService mealPlanService;
    private final CloudKitchenService cloudKitchenService;
    private final UserService userService;
    
    /**
     * Displays a list of meal plans, optionally filtered by cloud kitchen.
     * Adds the meal plans and cloud kitchen info to the model.
     */
    @GetMapping("/list")
    public String listMealPlans(@RequestParam(required = false) Long cloudKitchenId,
                                 Model model) {
        List<MealPlan> mealPlans;
        if (cloudKitchenId != null) {
            mealPlans = mealPlanService.findByCloudKitchenId(cloudKitchenId);
            Optional<CloudKitchen> cloudKitchenOpt = cloudKitchenService.findById(cloudKitchenId);
            if (cloudKitchenOpt.isPresent()) {
                model.addAttribute("cloudKitchen", cloudKitchenOpt.get());
            }
        } else {
            mealPlans = mealPlanService.findAll();
        }
        model.addAttribute("mealPlans", mealPlans);
        return "meal-plan/list";
    }
    
    /**
     * Displays the form to add a new meal plan, optionally for a specific cloud kitchen.
     * Adds available cloud kitchens to the model for selection.
     */
    @GetMapping("/add")
    public String addMealPlanForm(@RequestParam(required = false) Long cloudKitchenId,
                                   Model model) {
        MealPlan mealPlan = new MealPlan();
        if (cloudKitchenId != null) {
            Optional<CloudKitchen> cloudKitchenOpt = cloudKitchenService.findById(cloudKitchenId);
            if (cloudKitchenOpt.isPresent()) {
                mealPlan.setCloudKitchen(cloudKitchenOpt.get());
            }
        }
        model.addAttribute("mealPlan", mealPlan);
        
        // Get cloud kitchens for dropdown
        List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
        model.addAttribute("cloudKitchens", cloudKitchens);
        
        return "meal-plan/add";
    }
    
    /**
     * Handles the submission of the add meal plan form.
     * Validates input, checks ownership, and saves the meal plan if authorized.
     */
    @PostMapping("/add")
    public String addMealPlan(@Valid @ModelAttribute("mealPlan") MealPlan mealPlan,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
            model.addAttribute("cloudKitchens", cloudKitchens);
            return "meal-plan/add";
        }
        
        try {
            // Check if user is cloud kitchen owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    // Verify cloud kitchen ownership
                    if (mealPlan.getCloudKitchen() != null && 
                        mealPlan.getCloudKitchen().getOwner().getId().equals(userOpt.get().getId())) {
                        mealPlanService.save(mealPlan);
                        redirectAttributes.addFlashAttribute("success", "Meal plan added successfully!");
                        return "redirect:/meal-plan/list?cloudKitchenId=" + mealPlan.getCloudKitchen().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to add meal plans to this cloud kitchen!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding meal plan: " + e.getMessage());
        }
        return "redirect:/meal-plan/add";
    }
    
    /**
     * Displays the form to edit an existing meal plan by its ID.
     * Checks ownership and loads the meal plan and cloud kitchens for editing.
     */
    @GetMapping("/edit/{id}")
    public String editMealPlanForm(@PathVariable Long id, Model model) {
        Optional<MealPlan> mealPlanOpt = mealPlanService.findById(id);
        if (mealPlanOpt.isPresent()) {
            MealPlan mealPlan = mealPlanOpt.get();
            
            // Check if user is cloud kitchen owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    if (mealPlan.getCloudKitchen() != null && 
                        mealPlan.getCloudKitchen().getOwner().getId().equals(userOpt.get().getId())) {
                        model.addAttribute("mealPlan", mealPlan);
                        List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
                        model.addAttribute("cloudKitchens", cloudKitchens);
                        return "meal-plan/edit";
                    }
                }
            }
        }
        return "redirect:/access-denied";
    }
    
    /**
     * Handles the submission of the edit meal plan form.
     * Validates input, checks ownership, and updates the meal plan if authorized.
     */
    @PostMapping("/edit/{id}")
    public String editMealPlan(@PathVariable Long id,
                                @Valid @ModelAttribute("mealPlan") MealPlan mealPlan,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
            model.addAttribute("cloudKitchens", cloudKitchens);
            return "meal-plan/edit";
        }
        
        try {
            // Check if user is cloud kitchen owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    if (mealPlan.getCloudKitchen() != null && 
                        mealPlan.getCloudKitchen().getOwner().getId().equals(userOpt.get().getId())) {
                        mealPlan.setId(id);
                        mealPlanService.save(mealPlan);
                        redirectAttributes.addFlashAttribute("success", "Meal plan updated successfully!");
                        return "redirect:/meal-plan/list?cloudKitchenId=" + mealPlan.getCloudKitchen().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this meal plan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating meal plan: " + e.getMessage());
        }
        return "redirect:/meal-plan/edit/" + id;
    }
    
    /**
     * Deletes a meal plan by its ID if the user is authorized.
     * Checks ownership and removes the meal plan if permitted.
     */
    @PostMapping("/delete/{id}")
    public String deleteMealPlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<MealPlan> mealPlanOpt = mealPlanService.findById(id);
            if (mealPlanOpt.isPresent()) {
                MealPlan mealPlan = mealPlanOpt.get();
                
                // Check if user is cloud kitchen owner
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    Optional<User> userOpt = userService.findByEmail(auth.getName());
                    if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                        if (mealPlan.getCloudKitchen() != null && 
                            mealPlan.getCloudKitchen().getOwner().getId().equals(userOpt.get().getId())) {
                            mealPlanService.deleteById(id);
                            redirectAttributes.addFlashAttribute("success", "Meal plan deleted successfully!");
                            return "redirect:/meal-plan/list?cloudKitchenId=" + mealPlan.getCloudKitchen().getId();
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this meal plan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting meal plan: " + e.getMessage());
        }
        return "redirect:/meal-plan/list";
    }
    
    /**
     * Displays meal plans filtered by price range.
     * Adds the price range and meal plans to the model.
     */
    @GetMapping("/price-range")
    public String mealPlansByPriceRange(@RequestParam Double minPrice,
                                        @RequestParam Double maxPrice,
                                        Model model) {
        List<MealPlan> mealPlans = mealPlanService.findByPriceRange(minPrice, maxPrice);
        model.addAttribute("mealPlans", mealPlans);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "meal-plan/price-range";
    }
    
    /**
     * Displays meal plans filtered by duration type.
     * Adds the duration type and meal plans to the model.
     */
    @GetMapping("/duration/{durationType}")
    public String mealPlansByDuration(@PathVariable MealPlan.DurationType durationType,
                                      Model model) {
        List<MealPlan> mealPlans = mealPlanService.findByDurationType(durationType);
        model.addAttribute("mealPlans", mealPlans);
        model.addAttribute("durationType", durationType);
        return "meal-plan/duration";
    }
    
    /**
     * Displays meal plans for a specific cloud kitchen and duration type.
     * Adds the cloud kitchen, duration type, and meal plans to the model.
     */
    @GetMapping("/cloud-kitchen/{cloudKitchenId}/duration/{durationType}")
    public String mealPlansByCloudKitchenAndDuration(@PathVariable Long cloudKitchenId,
                                                     @PathVariable MealPlan.DurationType durationType,
                                                     Model model) {
        List<MealPlan> mealPlans = mealPlanService.findByCloudKitchenIdAndDurationType(cloudKitchenId, durationType);
        Optional<CloudKitchen> cloudKitchenOpt = cloudKitchenService.findById(cloudKitchenId);
        if (cloudKitchenOpt.isPresent()) {
            model.addAttribute("cloudKitchen", cloudKitchenOpt.get());
        }
        model.addAttribute("mealPlans", mealPlans);
        model.addAttribute("durationType", durationType);
        return "meal-plan/cloud-kitchen-duration";
    }
} 