package in.sp.main.repository;

import in.sp.main.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByHotelId(Long hotelId);
    
    List<Room> findByHotelIdAndIsAvailableTrue(Long hotelId);
    
    List<Room> findByRoomType(String roomType);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.roomType = :roomType")
    List<Room> findByHotelIdAndRoomType(@Param("hotelId") Long hotelId, @Param("roomType") String roomType);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.pricePerNight BETWEEN :minPrice AND :maxPrice")
    List<Room> findByHotelIdAndPriceRange(@Param("hotelId") Long hotelId, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.capacity >= :capacity")
    List<Room> findByHotelIdAndCapacityGreaterThanEqual(@Param("hotelId") Long hotelId, @Param("capacity") Integer capacity);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.isAvailable = true AND r.pricePerNight <= :maxPrice")
    List<Room> findByHotelIdAndAvailableAndPriceLessThanEqual(@Param("hotelId") Long hotelId, @Param("maxPrice") Double maxPrice);
} 