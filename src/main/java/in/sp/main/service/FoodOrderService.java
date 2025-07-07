package in.sp.main.service;

import in.sp.main.entity.FoodOrder;

import java.util.List;
import java.util.Optional;

public interface FoodOrderService {
    
    List<FoodOrder> findAll();
    
    Optional<FoodOrder> findById(Long id);
    
    FoodOrder createOrder(FoodOrder order);
    
    FoodOrder updateOrder(FoodOrder order);
    
    void deleteOrder(Long id);
    
    List<FoodOrder> findByUserId(Long userId);
    
    List<FoodOrder> findByRestaurantId(Long restaurantId);
    
    List<FoodOrder> findByStatus(FoodOrder.OrderStatus status);
    
    List<FoodOrder> findByUserIdAndStatus(Long userId, FoodOrder.OrderStatus status);
} 