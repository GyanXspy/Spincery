package in.sp.main.controller;

import in.sp.main.entity.CloudKitchenSubscription;
import in.sp.main.entity.CloudKitchen;
import in.sp.main.entity.User;
import in.sp.main.service.CloudKitchenSubscriptionService;
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
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class CloudKitchenSubscriptionController {
    
    private final CloudKitchenSubscriptionService subscriptionService;
    private final CloudKitchenService cloudKitchenService;
    private final UserService userService;
    
    @GetMapping("/list")
    public String listSubscriptions(@RequestParam(required = false) Long userId,
                                    @RequestParam(required = false) Long cloudKitchenId,
                                    Model model) {
        List<CloudKitchenSubscription> subscriptions;
        
        if (userId != null) {
            subscriptions = subscriptionService.findByUserId(userId);
        } else if (cloudKitchenId != null) {
            subscriptions = subscriptionService.findByCloudKitchenId(cloudKitchenId);
        } else {
            subscriptions = subscriptionService.findAll();
        }
        
        model.addAttribute("subscriptions", subscriptions);
        return "subscription/list";
    }
    
    @GetMapping("/add")
    public String addSubscriptionForm(@RequestParam(required = false) Long cloudKitchenId,
                                      Model model) {
        CloudKitchenSubscription subscription = new CloudKitchenSubscription();
        if (cloudKitchenId != null) {
            Optional<CloudKitchen> cloudKitchenOpt = cloudKitchenService.findById(cloudKitchenId);
            if (cloudKitchenOpt.isPresent()) {
                subscription.setCloudKitchen(cloudKitchenOpt.get());
            }
        }
        model.addAttribute("subscription", subscription);
        
        // Get cloud kitchens for dropdown
        List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
        model.addAttribute("cloudKitchens", cloudKitchens);
        
        return "subscription/add";
    }
    
    @PostMapping("/add")
    public String addSubscription(@Valid @ModelAttribute("subscription") CloudKitchenSubscription subscription,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
            model.addAttribute("cloudKitchens", cloudKitchens);
            return "subscription/add";
        }
        
        try {
            // Set the current user as the subscriber
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    subscription.setUser(userOpt.get());
                    subscriptionService.createSubscription(subscription);
                    redirectAttributes.addFlashAttribute("success", "Subscription created successfully!");
                    return "redirect:/subscription/list?userId=" + userOpt.get().getId();
                }
            }
            redirectAttributes.addFlashAttribute("error", "User not found!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating subscription: " + e.getMessage());
        }
        return "redirect:/subscription/add";
    }
    
    @GetMapping("/edit/{id}")
    public String editSubscriptionForm(@PathVariable Long id, Model model) {
        Optional<CloudKitchenSubscription> subscriptionOpt = subscriptionService.findById(id);
        if (subscriptionOpt.isPresent()) {
            CloudKitchenSubscription subscription = subscriptionOpt.get();
            
            // Check if user owns the subscription or is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() == User.UserRole.ADMIN || 
                        subscription.getUser().getId().equals(user.getId())) {
                        model.addAttribute("subscription", subscription);
                        List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
                        model.addAttribute("cloudKitchens", cloudKitchens);
                        return "subscription/edit";
                    }
                }
            }
        }
        return "redirect:/access-denied";
    }
    
    @PostMapping("/edit/{id}")
    public String editSubscription(@PathVariable Long id,
                                   @Valid @ModelAttribute("subscription") CloudKitchenSubscription subscription,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            List<CloudKitchen> cloudKitchens = cloudKitchenService.findAll();
            model.addAttribute("cloudKitchens", cloudKitchens);
            return "subscription/edit";
        }
        
        try {
            // Check if user owns the subscription or is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Optional<CloudKitchenSubscription> existingOpt = subscriptionService.findById(id);
                    if (existingOpt.isPresent() && 
                        (user.getRole() == User.UserRole.ADMIN || 
                         existingOpt.get().getUser().getId().equals(user.getId()))) {
                        subscription.setId(id);
                        subscriptionService.updateSubscription(subscription);
                        redirectAttributes.addFlashAttribute("success", "Subscription updated successfully!");
                        return "redirect:/subscription/list?userId=" + subscription.getUser().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this subscription!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating subscription: " + e.getMessage());
        }
        return "redirect:/subscription/edit/" + id;
    }
    
    @PostMapping("/delete/{id}")
    public String deleteSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<CloudKitchenSubscription> subscriptionOpt = subscriptionService.findById(id);
            if (subscriptionOpt.isPresent()) {
                CloudKitchenSubscription subscription = subscriptionOpt.get();
                
                // Check if user owns the subscription or is admin
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    Optional<User> userOpt = userService.findByEmail(auth.getName());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        if (user.getRole() == User.UserRole.ADMIN || 
                            subscription.getUser().getId().equals(user.getId())) {
                            subscriptionService.deleteSubscription(id);
                            redirectAttributes.addFlashAttribute("success", "Subscription deleted successfully!");
                            return "redirect:/subscription/list?userId=" + subscription.getUser().getId();
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this subscription!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting subscription: " + e.getMessage());
        }
        return "redirect:/subscription/list";
    }
    
    @GetMapping("/status/{status}")
    public String subscriptionsByStatus(@PathVariable CloudKitchenSubscription.SubscriptionStatus status,
                                       Model model) {
        List<CloudKitchenSubscription> subscriptions = subscriptionService.findByStatus(status);
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("status", status);
        return "subscription/status";
    }
    
    @GetMapping("/user/{userId}")
    public String subscriptionsByUser(@PathVariable Long userId, Model model) {
        List<CloudKitchenSubscription> subscriptions = subscriptionService.findByUserId(userId);
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
        }
        model.addAttribute("subscriptions", subscriptions);
        return "subscription/user-subscriptions";
    }
    
    @GetMapping("/cloud-kitchen/{cloudKitchenId}")
    public String subscriptionsByCloudKitchen(@PathVariable Long cloudKitchenId, Model model) {
        List<CloudKitchenSubscription> subscriptions = subscriptionService.findByCloudKitchenId(cloudKitchenId);
        Optional<CloudKitchen> cloudKitchenOpt = cloudKitchenService.findById(cloudKitchenId);
        if (cloudKitchenOpt.isPresent()) {
            model.addAttribute("cloudKitchen", cloudKitchenOpt.get());
        }
        model.addAttribute("subscriptions", subscriptions);
        return "subscription/cloud-kitchen-subscriptions";
    }
    
    @GetMapping("/details/{id}")
    public String subscriptionDetails(@PathVariable Long id, Model model) {
        Optional<CloudKitchenSubscription> subscriptionOpt = subscriptionService.findById(id);
        if (subscriptionOpt.isPresent()) {
            model.addAttribute("subscription", subscriptionOpt.get());
            return "subscription/details";
        }
        return "redirect:/subscription/list";
    }
} 