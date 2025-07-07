package in.sp.main.service.impl;

import in.sp.main.entity.Restaurant;
import in.sp.main.repository.RestaurantRepository;
import in.sp.main.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
} 