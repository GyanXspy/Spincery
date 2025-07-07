package in.sp.main.service;

import in.sp.main.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {
    
    List<OrderItem> findAll();
    
    Optional<OrderItem> findById(Long id);
    
    OrderItem save(OrderItem orderItem);
    
    void deleteById(Long id);
    
    List<OrderItem> findByFoodOrderId(Long foodOrderId);
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByRestaurantId(Long restaurantId);
    
    List<OrderItem> findByUserId(Long userId);
    
    List<OrderItem> findByMenuItemId(Long menuItemId);
} 