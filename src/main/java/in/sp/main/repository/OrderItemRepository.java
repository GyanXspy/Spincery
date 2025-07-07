package in.sp.main.repository;

import in.sp.main.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByFoodOrderId(Long foodOrderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.foodOrder.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.menuItem.restaurant.id = :restaurantId")
    List<OrderItem> findByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.foodOrder.user.id = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId")
    List<OrderItem> findByMenuItemId(@Param("menuItemId") Long menuItemId);
} 