package in.sp.main.controller;

import in.sp.main.entity.MenuItem;
import in.sp.main.entity.Restaurant;
import in.sp.main.entity.User;
import in.sp.main.service.MenuItemService;
import in.sp.main.service.RestaurantService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/menu-item")
@RequiredArgsConstructor
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;
    private final UserService userService;
    
    @GetMapping("/list")
    public String listMenuItems(@RequestParam(required = false) Long restaurantId,
                                Model model) {
        List<MenuItem> menuItems;
        if (restaurantId != null) {
            menuItems = menuItemService.findByRestaurantId(restaurantId);
            Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
            if (restaurantOpt.isPresent()) {
                model.addAttribute("restaurant", restaurantOpt.get());
            }
        } else {
            menuItems = menuItemService.findAll();
        }
        model.addAttribute("menuItems", menuItems);
        return "menu-item/list";
    }
    
    @GetMapping("/add")
    public String addMenuItemForm(@RequestParam(required = false) Long restaurantId,
                                  Model model) {
        MenuItem menuItem = new MenuItem();
        if (restaurantId != null) {
            Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
            if (restaurantOpt.isPresent()) {
                menuItem.setRestaurant(restaurantOpt.get());
            }
        }
        model.addAttribute("menuItem", menuItem);
        
        // Get restaurants owned by current user for dropdown
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Restaurant> restaurants = new ArrayList<>();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userService.findByEmail(auth.getName());
            if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                restaurants = restaurantService.findByOwnerId(userOpt.get().getId());
                model.addAttribute("ownerName", userOpt.get().getName());
            }
        }
        model.addAttribute("restaurants", restaurants);
        
        return "menu-item/add";
    }
    
    @PostMapping("/add")
    public String addMenuItem(@Valid @ModelAttribute("menuItem") MenuItem menuItem,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            // Get restaurants owned by current user for dropdown
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            List<Restaurant> restaurants = new ArrayList<>();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                    restaurants = restaurantService.findByOwnerId(userOpt.get().getId());
                    model.addAttribute("ownerName", userOpt.get().getName());
                }
            }
            model.addAttribute("restaurants", restaurants);
            return "menu-item/add";
        }
        
        try {
            // Check if user is restaurant owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                    // Verify restaurant ownership
                    if (menuItem.getRestaurant() != null && menuItem.getRestaurant().getOwner().getId().equals(userOpt.get().getId())) {
                        menuItemService.save(menuItem);
                        redirectAttributes.addFlashAttribute("success", "Menu item added successfully!");
                        return "redirect:/restaurant/menu?restaurantId=" + menuItem.getRestaurant().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to add menu items to this restaurant!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding menu item: " + e.getMessage());
        }
        return "redirect:/menu-item/add";
    }
    
    @GetMapping("/edit/{id}")
    public String editMenuItemForm(@PathVariable Long id, Model model) {
        Optional<MenuItem> menuItemOpt = menuItemService.findById(id);
        if (menuItemOpt.isPresent()) {
            MenuItem menuItem = menuItemOpt.get();
            
            // Check if user is restaurant owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                    if (menuItem.getRestaurant() != null && menuItem.getRestaurant().getOwner().getId().equals(userOpt.get().getId())) {
                        model.addAttribute("menuItem", menuItem);
                        // Get restaurants owned by current user for dropdown
                        List<Restaurant> restaurants = restaurantService.findByOwnerId(userOpt.get().getId());
                        model.addAttribute("restaurants", restaurants);
                        return "menu-item/edit";
                    }
                }
            }
        }
        return "redirect:/access-denied";
    }
    
    @PostMapping("/edit/{id}")
    public String editMenuItem(@PathVariable Long id,
                               @Valid @ModelAttribute("menuItem") MenuItem menuItem,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            // Get restaurants owned by current user for dropdown
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            List<Restaurant> restaurants = new ArrayList<>();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                    restaurants = restaurantService.findByOwnerId(userOpt.get().getId());
                }
            }
            model.addAttribute("restaurants", restaurants);
            return "menu-item/edit";
        }
        
        try {
            // Check if user is restaurant owner
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                    if (menuItem.getRestaurant() != null && menuItem.getRestaurant().getOwner().getId().equals(userOpt.get().getId())) {
                        menuItem.setId(id);
                        menuItemService.save(menuItem);
                        redirectAttributes.addFlashAttribute("success", "Menu item updated successfully!");
                        return "redirect:/restaurant/menu?restaurantId=" + menuItem.getRestaurant().getId();
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this menu item!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating menu item: " + e.getMessage());
        }
        return "redirect:/menu-item/edit/" + id;
    }
    
    @PostMapping("/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<MenuItem> menuItemOpt = menuItemService.findById(id);
            if (menuItemOpt.isPresent()) {
                MenuItem menuItem = menuItemOpt.get();
                
                // Check if user is restaurant owner
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    Optional<User> userOpt = userService.findByEmail(auth.getName());
                    if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.RESTAURANT_OWNER) {
                        if (menuItem.getRestaurant() != null && menuItem.getRestaurant().getOwner().getId().equals(userOpt.get().getId())) {
                            menuItemService.deleteById(id);
                            redirectAttributes.addFlashAttribute("success", "Menu item deleted successfully!");
                            return "redirect:/restaurant/menu?restaurantId=" + menuItem.getRestaurant().getId();
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this menu item!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting menu item: " + e.getMessage());
        }
        return "redirect:/restaurant/menu";
    }
    
    @GetMapping("/category/{category}")
    public String menuItemsByCategory(@PathVariable String category, Model model) {
        List<MenuItem> menuItems = menuItemService.findByCategory(category);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("category", category);
        return "menu-item/category";
    }
    
    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    public String menuItemsByRestaurantAndCategory(@PathVariable Long restaurantId,
                                                   @PathVariable String category,
                                                   Model model) {
        List<MenuItem> menuItems = menuItemService.findByRestaurantIdAndCategory(restaurantId, category);
        Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            model.addAttribute("restaurant", restaurantOpt.get());
        }
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("category", category);
        return "menu-item/restaurant-category";
    }
} 