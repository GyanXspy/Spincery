package in.sp.main.service;

import in.sp.main.entity.TableBooking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TableBookingService {
    
    List<TableBooking> findAll();
    
    Optional<TableBooking> findById(Long id);
    
    TableBooking createBooking(TableBooking booking);
    
    TableBooking updateBooking(TableBooking booking);
    
    void deleteBooking(Long id);
    
    List<TableBooking> findByUserId(Long userId);
    
    List<TableBooking> findByRestaurantId(Long restaurantId);
    
    List<TableBooking> findByStatus(TableBooking.BookingStatus status);
    
    boolean checkAvailability(Long restaurantId, LocalDate date, LocalTime time, Integer guests);
    
    List<TableBooking> findByRestaurantIdAndBookingDate(Long restaurantId, LocalDate bookingDate);
    
    List<TableBooking> findByRestaurantIdAndBookingDateRange(Long restaurantId, LocalDate startDate, LocalDate endDate);
} 