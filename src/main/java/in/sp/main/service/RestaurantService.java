package in.sp.main.service;

import in.sp.main.entity.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    
    List<Restaurant> findAll();
    
    Optional<Restaurant> findById(Long id);
    
    Restaurant save(Restaurant restaurant);
    
    void deleteById(Long id);
    
    List<Restaurant> findByCity(String city);
    
    List<Restaurant> findByOwnerId(Long ownerId);
    
    List<Restaurant> findActiveVerifiedByCity(String city);
    
    List<Restaurant> findByRestaurantNameContaining(String name);
    
    List<Restaurant> findDeliveryRestaurants();
    
    List<Restaurant> findDeliveryRestaurantsByCity(String city);
    
    List<Restaurant> findByRatingGreaterThanEqual(Double minRating);
    
    List<Restaurant> findByIsVerifiedTrue();
    
    // New methods for dynamic filtering
    List<Restaurant> findByCityAndRatingGreaterThanEqual(String city, Double minRating);
    
    List<Restaurant> findByCityAndCuisine(String city, String cuisine);
    
    List<Restaurant> findByCuisine(String cuisine);
    
    List<Restaurant> findByCityAndCuisineAndRatingGreaterThanEqual(String city, String cuisine, Double minRating);
} 