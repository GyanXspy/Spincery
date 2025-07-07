package in.sp.main.repository;

import in.sp.main.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    List<Hotel> findByCity(String city);
    
    List<Hotel> findByCityAndIsActiveTrue(String city);
    
    List<Hotel> findByIsVerifiedTrue();
    
    List<Hotel> findByIsActiveTrue();
    
    @Query("SELECT h FROM Hotel h WHERE h.city = :city AND h.isActive = true AND h.isVerified = true")
    List<Hotel> findActiveVerifiedByCity(@Param("city") String city);
    
    @Query("SELECT h FROM Hotel h WHERE h.owner.id = :ownerId")
    List<Hotel> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT h FROM Hotel h WHERE h.hotelName LIKE %:name% AND h.isActive = true")
    List<Hotel> findByHotelNameContaining(@Param("name") String name);
    
    @Query("SELECT h FROM Hotel h WHERE h.rating >= :rating AND h.isActive = true")
    List<Hotel> findByRatingGreaterThan(@Param("rating") Double rating);
    
    Optional<Hotel> findByEmail(String email);
    
    boolean existsByEmail(String email);
} 