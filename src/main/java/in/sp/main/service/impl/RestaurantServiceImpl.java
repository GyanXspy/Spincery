package in.sp.main.service.impl;

import in.sp.main.entity.Restaurant;
import in.sp.main.repository.RestaurantRepository;
import in.sp.main.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    
    @Override
    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }
    
    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findById(id);
    }
    
    @Override
    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }
    
    @Override
    public void deleteById(Long id) {
        restaurantRepository.deleteById(id);
    }
    
    @Override
    public List<Restaurant> findByCity(String city) {
        return restaurantRepository.findByCity(city);
    }
    
    @Override
    public List<Restaurant> findByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }
    
    @Override
    public List<Restaurant> findActiveVerifiedByCity(String city) {
        return restaurantRepository.findActiveVerifiedByCity(city);
    }
    
    @Override
    public List<Restaurant> findByRestaurantNameContaining(String name) {
        return restaurantRepository.findByRestaurantNameContaining(name);
    }
    
    @Override
    public List<Restaurant> findDeliveryRestaurants() {
        return restaurantRepository.findDeliveryRestaurants();
    }
    
    @Override
    public List<Restaurant> findDeliveryRestaurantsByCity(String city) {
        return restaurantRepository.findDeliveryRestaurantsByCity(city);
    }
    
    @Override
    public List<Restaurant> findByRatingGreaterThanEqual(Double minRating) {
        return restaurantRepository.findByRatingGreaterThanEqual(minRating);
    }
    
    @Override
    public List<Restaurant> findByIsVerifiedTrue() {
        return restaurantRepository.findByIsVerifiedTrue();
    }
    
    // New methods for dynamic filtering
    @Override
    public List<Restaurant> findByCityAndRatingGreaterThanEqual(String city, Double minRating) {
        return restaurantRepository.findByCityAndRatingGreaterThanEqual(city, minRating);
    }
    
    @Override
    public List<Restaurant> findByCityAndCuisine(String city, String cuisine) {
        return restaurantRepository.findByCityAndCuisine(city, cuisine);
    }
    
    @Override
    public List<Restaurant> findByCuisine(String cuisine) {
        return restaurantRepository.findByCuisine(cuisine);
    }
    
    @Override
    public List<Restaurant> findByCityAndCuisineAndRatingGreaterThanEqual(String city, String cuisine, Double minRating) {
        return restaurantRepository.findByCityAndCuisineAndRatingGreaterThanEqual(city, cuisine, minRating);
    }

    @PostConstruct
    public void migrateCoverPhotoUrl() {
        List<Restaurant> all = restaurantRepository.findAll();
        for (Restaurant r : all) {
            if ((r.getCoverPhotoUrl() == null || r.getCoverPhotoUrl().isBlank()) && r.getLogoUrl() != null && !r.getLogoUrl().isBlank()) {
                r.setCoverPhotoUrl(r.getLogoUrl());
                restaurantRepository.save(r);
            }
        }
    }
    
    @PostConstruct
    public void populateSampleData() {
        try {
            List<Restaurant> existingRestaurants = restaurantRepository.findAll();
            if (existingRestaurants.isEmpty()) {
                // Create sample restaurants with cuisine data
                Restaurant restaurant1 = new Restaurant();
                restaurant1.setRestaurantName("Pizza Palace");
                restaurant1.setOwnerName("John Doe");
                restaurant1.setEmail("pizza@example.com");
                restaurant1.setPhone("1234567890");
                restaurant1.setAddress("123 Main St");
                restaurant1.setCity("New York");
                restaurant1.setState("NY");
                restaurant1.setZipCode("10001");
                restaurant1.setDescription("Authentic Italian pizza and pasta");
                restaurant1.setCuisine("Italian");
                restaurant1.setRating(4.5);
                restaurant1.setTotalReviews(120);
                restaurant1.setAvgPreparationTime(30);
                restaurant1.setPackagingCharges(2.99);
                restaurant1.setDeliveryOffered(true);
                restaurant1.setVerified(true);
                restaurant1.setActive(true);
                // Note: owner is not set for sample data to avoid foreign key constraints
                restaurantRepository.save(restaurant1);
                
                Restaurant restaurant2 = new Restaurant();
                restaurant2.setRestaurantName("Golden Dragon");
                restaurant2.setOwnerName("Jane Smith");
                restaurant2.setEmail("dragon@example.com");
                restaurant2.setPhone("0987654321");
                restaurant2.setAddress("456 Oak Ave");
                restaurant2.setCity("Los Angeles");
                restaurant2.setState("CA");
                restaurant2.setZipCode("90210");
                restaurant2.setDescription("Traditional Chinese cuisine");
                restaurant2.setCuisine("Chinese");
                restaurant2.setRating(4.2);
                restaurant2.setTotalReviews(85);
                restaurant2.setAvgPreparationTime(25);
                restaurant2.setPackagingCharges(3.50);
                restaurant2.setDeliveryOffered(true);
                restaurant2.setVerified(true);
                restaurant2.setActive(true);
                // Note: owner is not set for sample data to avoid foreign key constraints
                restaurantRepository.save(restaurant2);
                
                Restaurant restaurant3 = new Restaurant();
                restaurant3.setRestaurantName("Taco Fiesta");
                restaurant3.setOwnerName("Carlos Rodriguez");
                restaurant3.setEmail("taco@example.com");
                restaurant3.setPhone("5551234567");
                restaurant3.setAddress("789 Pine St");
                restaurant3.setCity("Chicago");
                restaurant3.setState("IL");
                restaurant3.setZipCode("60601");
                restaurant3.setDescription("Authentic Mexican street food");
                restaurant3.setCuisine("Mexican");
                restaurant3.setRating(4.8);
                restaurant3.setTotalReviews(200);
                restaurant3.setAvgPreparationTime(20);
                restaurant3.setPackagingCharges(2.50);
                restaurant3.setDeliveryOffered(true);
                restaurant3.setVerified(true);
                restaurant3.setActive(true);
                // Note: owner is not set for sample data to avoid foreign key constraints
                restaurantRepository.save(restaurant3);
            }
        } catch (Exception e) {
            // Log error but don't fail startup
            System.err.println("Error populating sample data: " + e.getMessage());
        }
    }
} 