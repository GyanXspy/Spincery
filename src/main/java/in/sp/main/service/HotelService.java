package in.sp.main.service;

import in.sp.main.entity.Hotel;

import java.util.List;
import java.util.Optional;

public interface HotelService {
    
    List<Hotel> findAll();
    
    Optional<Hotel> findById(Long id);
    
    Hotel save(Hotel hotel);
    
    void deleteById(Long id);
    
    List<Hotel> findByCity(String city);
    
    List<Hotel> findByOwnerId(Long ownerId);
    
    List<Hotel> findByRatingGreaterThan(Double rating);
    
    List<Hotel> findActiveVerifiedByCity(String city);
    
    List<Hotel> findByHotelNameContaining(String name);
} 