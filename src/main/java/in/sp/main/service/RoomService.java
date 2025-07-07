package in.sp.main.service;

import in.sp.main.entity.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    
    List<Room> findAll();
    
    Optional<Room> findById(Long id);
    
    Room save(Room room);
    
    void deleteById(Long id);
    
    List<Room> findByHotelId(Long hotelId);
    
    List<Room> findByHotelIdAndIsAvailableTrue(Long hotelId);
    
    List<Room> findByRoomType(String roomType);
    
    List<Room> findByHotelIdAndRoomType(Long hotelId, String roomType);
    
    List<Room> findByHotelIdAndPriceRange(Long hotelId, Double minPrice, Double maxPrice);
    
    List<Room> findByHotelIdAndCapacityGreaterThanEqual(Long hotelId, Integer capacity);
    
    List<Room> findByHotelIdAndAvailableAndPriceLessThanEqual(Long hotelId, Double maxPrice);
} 