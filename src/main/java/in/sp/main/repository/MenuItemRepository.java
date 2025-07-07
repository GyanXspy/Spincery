package in.sp.main.repository;

import in.sp.main.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    List<MenuItem> findByRestaurantId(Long restaurantId);
    
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.category = :category")
    List<MenuItem> findByRestaurantIdAndCategory(@Param("restaurantId") Long restaurantId, @Param("category") String category);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.foodType = :foodType")
    List<MenuItem> findByRestaurantIdAndFoodType(@Param("restaurantId") Long restaurantId, @Param("foodType") MenuItem.FoodType foodType);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.dishName LIKE %:name%")
    List<MenuItem> findByRestaurantIdAndDishNameContaining(@Param("restaurantId") Long restaurantId, @Param("name") String name);
    
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.restaurant.id = :restaurantId")
    List<String> findCategoriesByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    List<MenuItem> findByCategory(String category);
} 