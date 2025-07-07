package in.sp.main.repository;

import in.sp.main.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    List<Restaurant> findByCity(String city);
    
    List<Restaurant> findByCityAndIsActiveTrue(String city);
    
    List<Restaurant> findByIsVerifiedTrue();
    
    List<Restaurant> findByIsActiveTrue();
    
    @Query("SELECT r FROM Restaurant r WHERE r.city = :city AND r.isActive = true AND r.isVerified = true")
    List<Restaurant> findActiveVerifiedByCity(@Param("city") String city);
    
    @Query("SELECT r FROM Restaurant r WHERE r.owner.id = :ownerId")
    List<Restaurant> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT r FROM Restaurant r WHERE r.restaurantName LIKE %:name% AND r.isActive = true")
    List<Restaurant> findByRestaurantNameContaining(@Param("name") String name);
    
    @Query("SELECT r FROM Restaurant r WHERE r.deliveryOffered = true AND r.isActive = true")
    List<Restaurant> findDeliveryRestaurants();
    
    @Query("SELECT r FROM Restaurant r WHERE r.deliveryOffered = true AND r.city = :city AND r.isActive = true")
    List<Restaurant> findDeliveryRestaurantsByCity(@Param("city") String city);
    
    @Query("SELECT r FROM Restaurant r WHERE r.rating >= :minRating AND r.isActive = true ORDER BY r.rating DESC")
    List<Restaurant> findByRatingGreaterThanEqual(@Param("minRating") Double minRating);
    
    Optional<Restaurant> findByEmail(String email);
    
    boolean existsByEmail(String email);
} 