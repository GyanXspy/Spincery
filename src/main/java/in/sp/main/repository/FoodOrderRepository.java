package in.sp.main.repository;

import in.sp.main.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    
    Optional<FoodOrder> findByOrderNumber(String orderNumber);
    
    List<FoodOrder> findByUserId(Long userId);
    
    List<FoodOrder> findByRestaurantId(Long restaurantId);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.user.id = :userId ORDER BY fo.createdAt DESC")
    List<FoodOrder> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.restaurant.id = :restaurantId ORDER BY fo.createdAt DESC")
    List<FoodOrder> findByRestaurantIdOrderByCreatedAtDesc(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.status = :status")
    List<FoodOrder> findByStatus(@Param("status") FoodOrder.OrderStatus status);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.restaurant.id = :restaurantId AND fo.status = :status")
    List<FoodOrder> findByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") FoodOrder.OrderStatus status);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.user.id = :userId AND fo.status = :status")
    List<FoodOrder> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FoodOrder.OrderStatus status);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.createdAt BETWEEN :startDate AND :endDate")
    List<FoodOrder> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT fo FROM FoodOrder fo WHERE fo.restaurant.id = :restaurantId AND fo.createdAt BETWEEN :startDate AND :endDate")
    List<FoodOrder> findByRestaurantIdAndCreatedAtBetween(@Param("restaurantId") Long restaurantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 