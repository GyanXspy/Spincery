package in.sp.main.controller;

import in.sp.main.entity.OrderItem;
import in.sp.main.entity.FoodOrder;
import in.sp.main.service.OrderItemService;
import in.sp.main.service.FoodOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order-item")
@RequiredArgsConstructor
public class OrderItemController {
    
    private final OrderItemService orderItemService;
    private final FoodOrderService foodOrderService;
    
    @GetMapping("/list")
    public String listOrderItems(@RequestParam(required = false) Long orderId,
                                 @RequestParam(required = false) Long restaurantId,
                                 @RequestParam(required = false) Long userId,
                                 Model model) {
        List<OrderItem> orderItems;
        
        if (orderId != null) {
            orderItems = orderItemService.findByFoodOrderId(orderId);
            Optional<FoodOrder> orderOpt = foodOrderService.findById(orderId);
            if (orderOpt.isPresent()) {
                model.addAttribute("order", orderOpt.get());
            }
        } else if (restaurantId != null) {
            orderItems = orderItemService.findByRestaurantId(restaurantId);
        } else if (userId != null) {
            orderItems = orderItemService.findByUserId(userId);
        } else {
            orderItems = orderItemService.findAll();
        }
        
        model.addAttribute("orderItems", orderItems);
        return "order-item/list";
    }
    
    @GetMapping("/order/{orderId}")
    public String orderItemsByOrder(@PathVariable Long orderId, Model model) {
        List<OrderItem> orderItems = orderItemService.findByFoodOrderId(orderId);
        Optional<FoodOrder> orderOpt = foodOrderService.findById(orderId);
        
        if (orderOpt.isPresent()) {
            model.addAttribute("order", orderOpt.get());
        }
        model.addAttribute("orderItems", orderItems);
        return "order-item/order-items";
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    public String orderItemsByRestaurant(@PathVariable Long restaurantId, Model model) {
        List<OrderItem> orderItems = orderItemService.findByRestaurantId(restaurantId);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("restaurantId", restaurantId);
        return "order-item/restaurant-items";
    }
    
    @GetMapping("/user/{userId}")
    public String orderItemsByUser(@PathVariable Long userId, Model model) {
        List<OrderItem> orderItems = orderItemService.findByUserId(userId);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("userId", userId);
        return "order-item/user-items";
    }
    
    @GetMapping("/menu-item/{menuItemId}")
    public String orderItemsByMenuItem(@PathVariable Long menuItemId, Model model) {
        List<OrderItem> orderItems = orderItemService.findByMenuItemId(menuItemId);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("menuItemId", menuItemId);
        return "order-item/menu-item-orders";
    }
    
    @GetMapping("/details/{id}")
    public String orderItemDetails(@PathVariable Long id, Model model) {
        Optional<OrderItem> orderItemOpt = orderItemService.findById(id);
        if (orderItemOpt.isPresent()) {
            model.addAttribute("orderItem", orderItemOpt.get());
            return "order-item/details";
        }
        return "redirect:/order-item/list";
    }
} 