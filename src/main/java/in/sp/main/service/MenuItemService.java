package in.sp.main.service;

import in.sp.main.entity.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemService {
    
    List<MenuItem> findAll();
    
    Optional<MenuItem> findById(Long id);
    
    MenuItem save(MenuItem menuItem);
    
    void deleteById(Long id);
    
    List<MenuItem> findByRestaurantId(Long restaurantId);
    
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
    
    List<MenuItem> findByCategory(String category);
    
    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category);
} 