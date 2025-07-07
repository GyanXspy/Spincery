package in.sp.main.repository;

import in.sp.main.entity.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
    
    List<RoomBooking> findByUserId(Long userId);
    
    List<RoomBooking> findByRoomId(Long roomId);
    
    @Query("SELECT rb FROM RoomBooking rb WHERE rb.room.hotel.id = :hotelId")
    List<RoomBooking> findByHotelId(@Param("hotelId") Long hotelId);
    
    List<RoomBooking> findByStatus(RoomBooking.BookingStatus status);
    
    List<RoomBooking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT rb FROM RoomBooking rb WHERE rb.room.id = :roomId AND " +
           "((rb.checkInDate <= :checkOut AND rb.checkOutDate >= :checkIn) OR " +
           "(rb.checkInDate >= :checkIn AND rb.checkInDate <= :checkOut))")
    List<RoomBooking> findByRoomIdAndDateRange(@Param("roomId") Long roomId, 
                                               @Param("checkIn") LocalDate checkIn, 
                                               @Param("checkOut") LocalDate checkOut);
    
    @Query("SELECT rb FROM RoomBooking rb WHERE rb.user.id = :userId AND rb.status = :status")
    List<RoomBooking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RoomBooking.BookingStatus status);
} 