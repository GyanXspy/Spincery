package in.sp.main.service;

import in.sp.main.entity.RoomBooking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomBookingService {
    
    List<RoomBooking> findAll();
    
    Optional<RoomBooking> findById(Long id);
    
    RoomBooking createBooking(RoomBooking booking);
    
    RoomBooking updateBooking(RoomBooking booking);
    
    void deleteBooking(Long id);
    
    List<RoomBooking> findByUserId(Long userId);
    
    List<RoomBooking> findByRoomId(Long roomId);
    
    List<RoomBooking> findByHotelId(Long hotelId);
    
    List<RoomBooking> findByStatus(RoomBooking.BookingStatus status);
    
    List<RoomBooking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<RoomBooking> findByRoomIdAndDateRange(Long roomId, LocalDate checkIn, LocalDate checkOut);
} 