package in.sp.main.controller;

import in.sp.main.entity.CloudKitchen;
import in.sp.main.entity.MealPlan;
import in.sp.main.entity.CloudKitchenSubscription;
import in.sp.main.entity.User;
import in.sp.main.service.CloudKitchenService;
import in.sp.main.service.MealPlanService;
import in.sp.main.service.CloudKitchenSubscriptionService;
import in.sp.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import in.sp.main.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/cloud-kitchen")
@RequiredArgsConstructor
public class CloudKitchenController {
    
    private final CloudKitchenService cloudKitchenService;
    private final MealPlanService mealPlanService;
    private final CloudKitchenSubscriptionService subscriptionService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    
    /**
     * Displays the cloud kitchen dashboard for the authenticated owner.
     * Loads user info and their cloud kitchens for the dashboard view.
     */
    @GetMapping("/dashboard")
    public String cloudKitchenDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    model.addAttribute("user", user);
                    model.addAttribute("userInitial", user.getName().substring(0, 1).toUpperCase());
                    List<CloudKitchen> kitchens = cloudKitchenService.findByOwnerId(user.getId());
                    model.addAttribute("kitchens", kitchens);
                    if (kitchens != null && kitchens.size() == 1) {
                        model.addAttribute("cloudKitchen", kitchens.get(0));
                    }
                    return "cloud-kitchen/dashboard";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Displays the cloud kitchen registration form.
     * Only accessible by CLOUD_KITCHEN_OWNER role.
     */
    @GetMapping("/register")
    public String cloudKitchenRegister(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    if (!model.containsAttribute("cloudKitchen")) {
                        model.addAttribute("cloudKitchen", new CloudKitchen());
                    }
                    return "cloud-kitchen/register";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    /**
     * Handles cloud kitchen registration form submission.
     * Only accessible by CLOUD_KITCHEN_OWNER role.
     */
    @PostMapping("/register")
    public String registerCloudKitchen(@ModelAttribute CloudKitchen cloudKitchen,
                                       @RequestParam("imageFile") MultipartFile imageFile,
                                       Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    try {
                        cloudKitchen.setOwner(user);
                        cloudKitchen.setOwnerName(user.getName());
                        cloudKitchen.setEmail(user.getEmail());
                        cloudKitchen.setPhone(user.getPhone());
                        String imageUrl = cloudinaryService.uploadFile(imageFile, "cloudkitchen");
                        cloudKitchen.setKitchenLogoUrl(imageUrl);
                        cloudKitchenService.save(cloudKitchen);
                        model.addAttribute("success", "Cloud Kitchen registered successfully!");
                    } catch (Exception e) {
                        model.addAttribute("error", "Error registering Cloud Kitchen: " + e.getMessage());
                    }
                    return "cloud-kitchen/register";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Displays a list of cloud kitchens, optionally filtered by city.
     * Adds the kitchens and city info to the model.
     */
    @GetMapping("/list")
    public String cloudKitchenList(@RequestParam(required = false) String city, Model model) {
        List<CloudKitchen> kitchens;
        try {
            if (city != null && !city.trim().isEmpty()) {
                kitchens = cloudKitchenService.findActiveVerifiedByCity(city);
            } else {
                kitchens = cloudKitchenService.findByIsVerifiedTrue();
            }
            kitchens = kitchens.stream().filter(CloudKitchen::isActive).toList();
        } catch (Exception e) {
            kitchens = new ArrayList<>();
            model.addAttribute("error", "Error loading cloud kitchens: " + e.getMessage());
        }
        model.addAttribute("kitchens", kitchens);
        model.addAttribute("city", city != null ? city : "");
        return "cloud-kitchen/kitchens";
    }
    
    /**
     * Displays the details of a specific cloud kitchen by its ID.
     * Adds the kitchen to the model or redirects if not found.
     */
    @GetMapping("/details")
    public String cloudKitchenDetails(@RequestParam(required = false) Long kitchenId, Model model) {
        if (kitchenId == null) {
            return "redirect:/cloud-kitchen/list";
        }
        
        Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
        if (kitchenOpt.isPresent()) {
            CloudKitchen kitchen = kitchenOpt.get();
            model.addAttribute("kitchen", kitchen);
            return "cloud-kitchen/details";
        } else {
            model.addAttribute("error", "Cloud kitchen not found");
            return "redirect:/cloud-kitchen/list";
        }
    }
    
    /**
     * Displays the main cloud kitchen page with a list of all cloud kitchens.
     */
    @GetMapping("/cloud-kitchen")
    public String cloudKitchenPage(Model model) {
        try {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findByIsVerifiedTrue();
            cloudKitchens = cloudKitchens.stream().filter(CloudKitchen::isActive).toList();
            model.addAttribute("cloudKitchens", cloudKitchens);
        } catch (Exception e) {
            model.addAttribute("cloudKitchens", new ArrayList<>());
            model.addAttribute("error", "Error loading cloud kitchens: " + e.getMessage());
        }
        // Add logged-in user to the model
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            userOpt.ifPresent(user -> model.addAttribute("user", user));
        }
        return "cloud-kitchen/index";
    }
    
    /**
     * Displays the cloud kitchens page with a list of all cloud kitchens.
     */
    @GetMapping("/cloud-kitchens")
    public String cloudKitchensPage(Model model) {
        try {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findByIsVerifiedTrue();
            cloudKitchens = cloudKitchens.stream().filter(CloudKitchen::isActive).toList();
            model.addAttribute("cloudKitchens", cloudKitchens);
        } catch (Exception e) {
            model.addAttribute("cloudKitchens", new ArrayList<>());
            model.addAttribute("error", "Error loading cloud kitchens: " + e.getMessage());
        }
        return "cloud-kitchen/cloud-kitchens";
    }
    
    /**
     * Displays the details of a specific cloud kitchen by path variable ID, including its meal plans.
     */
    @GetMapping("/cloud-kitchen-details/{id}")
    public String cloudKitchenDetailsById(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/cloud-kitchens";
        }
        
        Optional<CloudKitchen> cloudKitchenOpt = cloudKitchenService.findById(id);
        if (cloudKitchenOpt.isPresent()) {
            CloudKitchen cloudKitchen = cloudKitchenOpt.get();
            try {
                List<MealPlan> mealPlans = mealPlanService.findByCloudKitchenId(id);
                model.addAttribute("cloudKitchen", cloudKitchen);
                model.addAttribute("mealPlans", mealPlans);
            } catch (Exception e) {
                model.addAttribute("cloudKitchen", cloudKitchen);
                model.addAttribute("mealPlans", new ArrayList<>());
                model.addAttribute("error", "Error loading meal plans: " + e.getMessage());
            }
            return "cloud-kitchen/cloud-kitchen-details";
        }
        return "redirect:/cloud-kitchens";
    }
    
    /**
     * Displays the meal plans for a specific cloud kitchen by its ID.
     */
    @GetMapping("/{kitchenId}/meal-plans")
    public String mealPlans(@PathVariable Long kitchenId, Model model) {
        Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
        if (kitchenOpt.isPresent()) {
            CloudKitchen kitchen = kitchenOpt.get();
            model.addAttribute("cloudKitchen", kitchen);
            List<MealPlan> mealPlans = mealPlanService.findByCloudKitchenId(kitchenId);
            model.addAttribute("mealPlans", mealPlans);
            return "cloud-kitchen/meal-plans";
        }
        model.addAttribute("error", "Cloud kitchen not found.");
        return "cloud-kitchen/meal-plans";
    }
    
    /**
     * Displays the subscription form for a specific meal plan.
     * Adds the meal plan and a new subscription object to the model.
     */
    @GetMapping("/meal-plan/{mealPlanId}/subscribe")
    public String subscribeToMealPlan(@PathVariable Long mealPlanId, Model model) {
        if (mealPlanId == null) {
            return "redirect:/cloud-kitchens";
        }
        
        Optional<MealPlan> mealPlanOpt = mealPlanService.findById(mealPlanId);
        if (mealPlanOpt.isPresent()) {
            MealPlan mealPlan = mealPlanOpt.get();
            model.addAttribute("mealPlan", mealPlan);
            model.addAttribute("subscription", new CloudKitchenSubscription());
            return "cloud-kitchen/subscribe";
        }
        return "redirect:/cloud-kitchens";
    }
    
    /**
     * Handles the creation of a new cloud kitchen meal plan subscription.
     * Associates the subscription with the authenticated user and saves it.
     */
    @PostMapping("/subscription/create")
    public String createSubscription(
            @RequestParam Long mealPlanId,
            @RequestParam Long cloudKitchenId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String deliveryAddress,
            @RequestParam String deliveryTimeSlots,
            @RequestParam(required = false) String dietaryPreferences,
            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Optional<MealPlan> mealPlanOpt = mealPlanService.findById(mealPlanId);
                Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(cloudKitchenId);
                if (mealPlanOpt.isEmpty() || kitchenOpt.isEmpty()) {
                    model.addAttribute("error", "Invalid meal plan or kitchen.");
                    if (mealPlanOpt.isPresent()) model.addAttribute("mealPlan", mealPlanOpt.get());
                    return "cloud-kitchen/subscribe";
                }
                CloudKitchenSubscription subscription = new CloudKitchenSubscription();
                subscription.setUser(user);
                subscription.setMealPlan(mealPlanOpt.get());
                subscription.setCloudKitchen(kitchenOpt.get());
                subscription.setStartDate(java.time.LocalDate.parse(startDate));
                subscription.setEndDate(java.time.LocalDate.parse(endDate));
                subscription.setDeliveryAddress(deliveryAddress);
                subscription.setDeliveryTimeSlots(deliveryTimeSlots);
                subscription.setDietaryPreferences(dietaryPreferences);
                try {
                    CloudKitchenSubscription savedSubscription = subscriptionService.createSubscription(subscription);
                    model.addAttribute("subscription", savedSubscription);
                    return "cloud-kitchen/subscription-confirmation";
                } catch (Exception e) {
                    model.addAttribute("error", "Error creating subscription: " + e.getMessage());
                    model.addAttribute("mealPlan", mealPlanOpt.get());
                    return "cloud-kitchen/subscribe";
                }
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/subscriptions")
    public String userSubscriptions(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                try {
                    List<CloudKitchenSubscription> subscriptions = subscriptionService.findByUserId(user.getId());
                    model.addAttribute("subscriptions", subscriptions);
                } catch (Exception e) {
                    model.addAttribute("subscriptions", new ArrayList<>());
                    model.addAttribute("error", "Error loading subscriptions: " + e.getMessage());
                }
                return "cloud-kitchen/subscriptions";
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/subscription/{id}")
    public String subscriptionDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/cloud-kitchen/subscriptions";
        }
        
        Optional<CloudKitchenSubscription> subscriptionOpt = subscriptionService.findById(id);
        if (subscriptionOpt.isPresent()) {
            CloudKitchenSubscription subscription = subscriptionOpt.get();
            model.addAttribute("subscription", subscription);
            return "cloud-kitchen/subscription-details";
        }
        return "redirect:/cloud-kitchen/subscriptions";
    }
    
    @GetMapping("/search-cloud-kitchens")
    public String searchCloudKitchens(@RequestParam String city, Model model) {
        try {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findByCity(city);
            model.addAttribute("cloudKitchens", cloudKitchens);
        } catch (Exception e) {
            model.addAttribute("cloudKitchens", new ArrayList<>());
            model.addAttribute("error", "Error searching cloud kitchens: " + e.getMessage());
        }
        model.addAttribute("city", city);
        return "cloud-kitchen/search-results";
    }
    
    @GetMapping("/analytics")
    public String cloudKitchenAnalytics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    model.addAttribute("user", user);
                    // Add analytics data here
                    return "cloud-kitchen/analytics";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/settings")
    public String cloudKitchenSettings(@RequestParam("kitchenId") Long kitchenId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
                    if (kitchenOpt.isPresent()) {
                        model.addAttribute("cloudKitchen", kitchenOpt.get());
                        return "cloud-kitchen/settings";
                    }
                    model.addAttribute("error", "Cloud kitchen not found.");
                    return "cloud-kitchen/settings";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/schedule")
    public String cloudKitchenSchedule(@RequestParam("kitchenId") Long kitchenId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
                    if (kitchenOpt.isPresent()) {
                        model.addAttribute("cloudKitchen", kitchenOpt.get());
                        return "cloud-kitchen/schedule";
                    }
                    model.addAttribute("error", "Cloud kitchen not found.");
                    return "cloud-kitchen/schedule";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/delivery")
    public String cloudKitchenDelivery(@RequestParam("kitchenId") Long kitchenId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
                    if (kitchenOpt.isPresent()) {
                        model.addAttribute("cloudKitchen", kitchenOpt.get());
                        return "cloud-kitchen/delivery";
                    }
                    model.addAttribute("error", "Cloud kitchen not found.");
                    return "cloud-kitchen/delivery";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/meal-plans")
    public String mealPlansPage(Model model) {
        try {
            List<MealPlan> mealPlans = mealPlanService.findAll();
            model.addAttribute("mealPlans", mealPlans);
        } catch (Exception e) {
            model.addAttribute("mealPlans", new ArrayList<>());
            model.addAttribute("error", "Error loading meal plans: " + e.getMessage());
        }
        return "cloud-kitchen/meal-plans";
    }

    @GetMapping("/{kitchenId}/meal-plans/add")
    public String showAddMealPlanForm(@PathVariable Long kitchenId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
                    if (kitchenOpt.isEmpty()) {
                        model.addAttribute("error", "Cloud kitchen not found.");
                        return "redirect:/cloud-kitchen/" + kitchenId + "/meal-plans";
                    }
                    model.addAttribute("cloudKitchen", kitchenOpt.get());
                    model.addAttribute("mealPlan", new MealPlan());
                    model.addAttribute("durationTypes", MealPlan.DurationType.values());
                    return "cloud-kitchen/meal-plan/add";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/{kitchenId}/meal-plans/add")
    public String addMealPlan(@PathVariable Long kitchenId, @ModelAttribute("mealPlan") MealPlan mealPlan, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.UserRole.CLOUD_KITCHEN_OWNER) {
                    Optional<CloudKitchen> kitchenOpt = cloudKitchenService.findById(kitchenId);
                    if (kitchenOpt.isEmpty()) {
                        model.addAttribute("error", "Cloud kitchen not found.");
                        return "redirect:/cloud-kitchen/" + kitchenId + "/meal-plans";
                    }
                    mealPlan.setCloudKitchen(kitchenOpt.get());
                    mealPlanService.save(mealPlan);
                    return "redirect:/cloud-kitchen/" + kitchenId + "/meal-plans";
                } else {
                    return "redirect:/access-denied";
                }
            }
        }
        return "redirect:/login";
    }
} 