package in.sp.main.controller;

import in.sp.main.entity.FoodOrder;
import in.sp.main.entity.MenuItem;
import in.sp.main.entity.Restaurant;
import in.sp.main.entity.User;
import in.sp.main.service.FoodOrderService;
import in.sp.main.service.MenuItemService;
import in.sp.main.service.RestaurantService;
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
import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class FoodDeliveryController {
    
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final FoodOrderService foodOrderService;
    private final UserService userService;
    
    /**
     * Displays the main food delivery page with a list of all restaurants.
     * Handles errors in loading restaurants gracefully.
     */
    @GetMapping("/food-delivery")
    public String foodDeliveryPage(Model model) {
        try {
            List<Restaurant> restaurants = restaurantService.findAll();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
        }
        return "food-delivery/index";
    }
    
    /**
     * Displays the list of all restaurants for food delivery.
     * Handles search parameters and filters restaurants accordingly.
     */
    @GetMapping("/restaurants")
    public String restaurantsPage(@RequestParam(required = false) String city,
                                @RequestParam(required = false) String cuisine,
                                @RequestParam(required = false) String rating,
                                Model model) {
        try {
            List<Restaurant> restaurants;
            
            // Handle different search combinations
            if (city != null && !city.trim().isEmpty() && cuisine != null && !cuisine.trim().isEmpty() && rating != null && !rating.trim().isEmpty()) {
                // All three parameters
                Double minRating = Double.parseDouble(rating);
                restaurants = restaurantService.findByCityAndCuisineAndRatingGreaterThanEqual(city.trim(), cuisine.trim(), minRating);
            } else if (city != null && !city.trim().isEmpty() && cuisine != null && !cuisine.trim().isEmpty()) {
                // City and cuisine
                restaurants = restaurantService.findByCityAndCuisine(city.trim(), cuisine.trim());
            } else if (city != null && !city.trim().isEmpty() && rating != null && !rating.trim().isEmpty()) {
                // City and rating
                Double minRating = Double.parseDouble(rating);
                restaurants = restaurantService.findByCityAndRatingGreaterThanEqual(city.trim(), minRating);
            } else if (cuisine != null && !cuisine.trim().isEmpty() && rating != null && !rating.trim().isEmpty()) {
                // Cuisine and rating
                Double minRating = Double.parseDouble(rating);
                List<Restaurant> cuisineRestaurants = restaurantService.findByCuisine(cuisine.trim());
                restaurants = cuisineRestaurants.stream()
                    .filter(r -> r.getRating() >= minRating)
                    .toList();
            } else if (city != null && !city.trim().isEmpty()) {
                // Only city
                restaurants = restaurantService.findByCity(city.trim());
            } else if (cuisine != null && !cuisine.trim().isEmpty()) {
                // Only cuisine
                restaurants = restaurantService.findByCuisine(cuisine.trim());
            } else if (rating != null && !rating.trim().isEmpty()) {
                // Only rating
                Double minRating = Double.parseDouble(rating);
                restaurants = restaurantService.findByRatingGreaterThanEqual(minRating);
            } else {
                // No filters - get all verified restaurants
                restaurants = restaurantService.findByIsVerifiedTrue();
            }
            
            // Filter to only show active and verified restaurants
            restaurants = restaurants.stream()
                .filter(Restaurant::isActive)
                .filter(Restaurant::isVerified)
                .toList();
            
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("city", city != null ? city : "");
            model.addAttribute("cuisine", cuisine != null ? cuisine : "");
            model.addAttribute("rating", rating != null ? rating : "");
            
        } catch (Exception e) {
            model.addAttribute("restaurants", new ArrayList<>());
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
            model.addAttribute("city", city != null ? city : "");
            model.addAttribute("cuisine", cuisine != null ? cuisine : "");
            model.addAttribute("rating", rating != null ? rating : "");
        }
        return "food-delivery/restaurants";
    }
    
    /**
     * Displays the details of a specific restaurant, including its menu items.
     * Handles errors in loading menu items gracefully.
     */
    @GetMapping("/restaurant-details/{id}")
    public String restaurantDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/restaurants";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(id);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            try {
                List<MenuItem> menuItems = menuItemService.findByRestaurantId(id);
                model.addAttribute("restaurant", restaurant);
                model.addAttribute("menuItems", menuItems);
            } catch (Exception e) {
                model.addAttribute("restaurant", restaurant);
                model.addAttribute("menuItems", new ArrayList<>());
                model.addAttribute("error", "Error loading menu items: " + e.getMessage());
            }
            return "food-delivery/restaurant-details";
        }
        return "redirect:/restaurants";
    }
    
    /**
     * Displays the menu for a specific restaurant by its ID.
     * Handles errors in loading menu items gracefully.
     */
    @GetMapping("/restaurant/{restaurantId}/menu")
    public String restaurantMenu(@PathVariable Long restaurantId, Model model) {
        if (restaurantId == null) {
            return "redirect:/restaurants";
        }
        
        Optional<Restaurant> restaurantOpt = restaurantService.findById(restaurantId);
        List<MenuItem> menuItems = new ArrayList<>();
        
        try {
            menuItems = menuItemService.findByRestaurantId(restaurantId);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading menu items: " + e.getMessage());
        }
        
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("menuItems", menuItems);
            return "food-delivery/menu";
        }
        return "redirect:/restaurants";
    }
    
    /**
     * Handles order creation for food delivery.
     * Associates the order with the authenticated user and saves it.
     * Displays order confirmation or error messages as appropriate.
     */
    @PostMapping("/order/create")
    public String createOrder(@ModelAttribute FoodOrder order, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                order.setUser(user);
                // Block order if restaurant is closed
                if (order.getRestaurant() != null && (order.getRestaurant().getIsOpen() == null || !order.getRestaurant().getIsOpen())) {
                    model.addAttribute("error", "This restaurant is currently closed and cannot accept orders.");
                    model.addAttribute("restaurant", order.getRestaurant());
                    return "food-delivery/menu";
                }
                try {
                    FoodOrder savedOrder = foodOrderService.createOrder(order);
                    model.addAttribute("order", savedOrder);
                    return "food-delivery/order-confirmation";
                } catch (Exception e) {
                    model.addAttribute("error", "Error creating order: " + e.getMessage());
                    return "food-delivery/menu";
                }
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Displays the list of orders for the authenticated user.
     * Loads all food orders placed by the user.
     */
    @GetMapping("/orders")
    public String userOrders(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                try {
                    List<FoodOrder> orders = foodOrderService.findByUserId(user.getId());
                    model.addAttribute("orders", orders);
                } catch (Exception e) {
                    model.addAttribute("orders", new ArrayList<>());
                    model.addAttribute("error", "Error loading orders: " + e.getMessage());
                }
                return "food-delivery/orders";
            }
        }
        return "redirect:/login";
    }
    
    /**
     * Displays the details of a specific food order by its ID.
     * Shows order details or redirects if not found.
     */
    @GetMapping("/order-details/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        if (id == null) {
            return "redirect:/orders";
        }
        
        Optional<FoodOrder> orderOpt = foodOrderService.findById(id);
        if (orderOpt.isPresent()) {
            FoodOrder order = orderOpt.get();
            model.addAttribute("order", order);
            return "food-delivery/order-details";
        }
        return "redirect:/orders";
    }
    
    /**
     * Displays the order tracking page (form for entering order ID).
     */
    @GetMapping("/track-order")
    public String trackOrderPage() {
        return "food-delivery/track-order";
    }
    
    /**
     * Displays the tracking details for a specific order by its ID.
     * Shows order tracking info or redirects if not found.
     */
    @GetMapping("/track-order/{orderId}")
    public String trackOrder(@PathVariable Long orderId, Model model) {
        if (orderId == null) {
            return "redirect:/track-order";
        }
        
        Optional<FoodOrder> orderOpt = foodOrderService.findById(orderId);
        if (orderOpt.isPresent()) {
            FoodOrder order = orderOpt.get();
            model.addAttribute("order", order);
            return "food-delivery/tracking";
        }
        return "redirect:/track-order";
    }

    /**
     * Handles adding a menu item to the user's cart (session-based).
     */
    @PostMapping("/food-delivery/cart/add")
    public String addToCart(@RequestParam Long menuItemId, @RequestParam(defaultValue = "1") Integer quantity, HttpSession session, @RequestHeader(value = "referer", required = false) String referer) {
        // Retrieve or create cart from session
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        // Check if item already in cart
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getMenuItemId().equals(menuItemId)) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(menuItemId, quantity));
        }
        session.setAttribute("cart", cart);
        // Redirect back to previous page or restaurant details
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/food-delivery";
    }

    /**
     * Remove an item from the cart.
     */
    @PostMapping("/food-delivery/cart/remove")
    public String removeFromCart(@RequestParam Long menuItemId, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getMenuItemId().equals(menuItemId));
            session.setAttribute("cart", cart);
        }
        return "redirect:/food-delivery/cart";
    }

    /**
     * Update the quantity of an item in the cart.
     */
    @PostMapping("/food-delivery/cart/update")
    public String updateCartItem(@RequestParam Long menuItemId, @RequestParam Integer quantity, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (item.getMenuItemId().equals(menuItemId)) {
                    item.setQuantity(quantity);
                    break;
                }
            }
            session.setAttribute("cart", cart);
        }
        return "redirect:/food-delivery/cart";
    }

    /**
     * Checkout: clear the cart and show a confirmation page.
     */
    @PostMapping("/food-delivery/cart/checkout")
    public String checkoutCart(HttpSession session, Model model) {
        // Get user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User user = null;
        if (email != null && !"anonymousUser".equals(email)) {
            user = userService.findByEmail(email).orElse(null);
        }
        // Get payment methods from the first restaurant in the cart
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        String paymentMethods = null;
        if (cart != null && !cart.isEmpty()) {
            MenuItem menuItem = menuItemService.findById(cart.get(0).getMenuItemId()).orElse(null);
            if (menuItem != null && menuItem.getRestaurant() != null) {
                paymentMethods = menuItem.getRestaurant().getAcceptedPaymentMethods();
            }
        }
        // Do NOT clear the cart here
        model.addAttribute("message", "Order placed successfully! Thank you for your purchase.");
        model.addAttribute("user", user);
        model.addAttribute("paymentMethods", paymentMethods);
        return "food-delivery/checkout-confirmation";
    }

    @PostMapping("/food-delivery/checkout-confirmation")
    public String confirmPaymentMethod(@RequestParam String paymentMethod, Model model, HttpSession session) {
        if ("Online Payment".equals(paymentMethod)) {
            return "redirect:/food-delivery/payment";
        }
        // Get user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User user = null;
        if (email != null && !"anonymousUser".equals(email)) {
            user = userService.findByEmail(email).orElse(null);
        }
        // Get payment methods and bank/UPI details from the first restaurant in the cart
        String paymentMethods = null;
        String bankAccountHolder = null;
        String bankName = null;
        String accountNumber = null;
        String ifscCode = null;
        String upiId = null;
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null && !cart.isEmpty()) {
            MenuItem menuItem = menuItemService.findById(cart.get(0).getMenuItemId()).orElse(null);
            if (menuItem != null && menuItem.getRestaurant() != null) {
                Restaurant restaurant = menuItem.getRestaurant();
                paymentMethods = restaurant.getAcceptedPaymentMethods();
                bankAccountHolder = restaurant.getBankAccountHolder();
                bankName = restaurant.getBankName();
                accountNumber = restaurant.getAccountNumber();
                ifscCode = restaurant.getIfscCode();
                upiId = restaurant.getUpiId();
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("selectedPaymentMethod", paymentMethod);
        if ("Online Payment".equals(paymentMethod)) {
            model.addAttribute("bankAccountHolder", bankAccountHolder);
            model.addAttribute("bankName", bankName);
            model.addAttribute("accountNumber", accountNumber);
            model.addAttribute("ifscCode", ifscCode);
            model.addAttribute("upiId", upiId);
        }
        return "food-delivery/checkout-confirmation";
    }

    @GetMapping("/food-delivery/cart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        List<CartItemWithMenuItem> cartItems = new ArrayList<>();
        double total = 0;
        for (CartItem item : cart) {
            MenuItem menuItem = menuItemService.findById(item.getMenuItemId()).orElse(null);
            if (menuItem != null) {
                cartItems.add(new CartItemWithMenuItem(menuItem, item.getQuantity()));
                total += menuItem.getPrice() * item.getQuantity();
            }
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "food-delivery/cart";
    }

    @GetMapping("/food-delivery/payment")
    public String paymentPage(HttpSession session, Model model) {
        // Get user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        User user = null;
        if (email != null && !"anonymousUser".equals(email)) {
            user = userService.findByEmail(email).orElse(null);
        }
        // Get total and owner payment details from cart
        List<CartItem> cartRaw = (List<CartItem>) session.getAttribute("cart");
        double total = 0;
        String upiId = null, bankAccountHolder = null, bankName = null, accountNumber = null, ifscCode = null;
        List<CartItemWithMenuItem> cart = new ArrayList<>();
        if (cartRaw != null && !cartRaw.isEmpty()) {
            for (CartItem item : cartRaw) {
                MenuItem menuItem = menuItemService.findById(item.getMenuItemId()).orElse(null);
                if (menuItem != null) {
                    total += menuItem.getPrice() * item.getQuantity();
                    cart.add(new CartItemWithMenuItem(menuItem, item.getQuantity()));
                    if (upiId == null && menuItem.getRestaurant() != null) {
                        Restaurant r = menuItem.getRestaurant();
                        upiId = r.getUpiId();
                        bankAccountHolder = r.getBankAccountHolder();
                        bankName = r.getBankName();
                        accountNumber = r.getAccountNumber();
                        ifscCode = r.getIfscCode();
                    }
                }
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("total", total);
        model.addAttribute("cart", cart);
        model.addAttribute("upiId", upiId);
        model.addAttribute("bankAccountHolder", bankAccountHolder);
        model.addAttribute("bankName", bankName);
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("ifscCode", ifscCode);
        return "food-delivery/payment";
    }

    @PostMapping("/food-delivery/payment")
    public String confirmPayment(@RequestParam String paymentMethod, @RequestParam(required = false) String payerUpiId, Model model, HttpSession session) {
        // Clear the cart only after payment is confirmed
        session.removeAttribute("cart");
        model.addAttribute("selectedPaymentMethod", paymentMethod);
        model.addAttribute("payerUpiId", payerUpiId);
        model.addAttribute("message", "Payment successful! Thank you for your order.");
        return "food-delivery/payment-confirmation";
    }

    // Helper class for cart items
    public static class CartItem {
        private Long menuItemId;
        private Integer quantity;
        public CartItem(Long menuItemId, Integer quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }
        public Long getMenuItemId() { return menuItemId; }
        public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CartItemWithMenuItem {
        private MenuItem menuItem;
        private int quantity;
        public CartItemWithMenuItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }
        public MenuItem getMenuItem() { return menuItem; }
        public int getQuantity() { return quantity; }
    }
} 