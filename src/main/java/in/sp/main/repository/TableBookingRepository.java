package in.sp.main.repository;

import in.sp.main.entity.TableBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Long> {
    
    List<TableBooking> findByUserId(Long userId);
    
    List<TableBooking> findByRestaurantId(Long restaurantId);
    
    List<TableBooking> findByStatus(TableBooking.BookingStatus status);
    
    List<TableBooking> findByRestaurantIdAndBookingDate(Long restaurantId, LocalDate bookingDate);
    
    @Query("SELECT tb FROM TableBooking tb WHERE tb.restaurant.id = :restaurantId AND tb.bookingDate = :bookingDate AND tb.bookingTime = :bookingTime")
    List<TableBooking> findByRestaurantIdAndBookingDateAndBookingTime(@Param("restaurantId") Long restaurantId, 
                                                        @Param("bookingDate") LocalDate bookingDate, 
                                                        @Param("bookingTime") LocalTime bookingTime);
    
    @Query("SELECT tb FROM TableBooking tb WHERE tb.restaurant.id = :restaurantId AND tb.bookingDate BETWEEN :startDate AND :endDate")
    List<TableBooking> findByRestaurantIdAndBookingDateBetween(@Param("restaurantId") Long restaurantId, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT tb FROM TableBooking tb WHERE tb.user.id = :userId AND tb.status = :status")
    List<TableBooking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TableBooking.BookingStatus status);
} 