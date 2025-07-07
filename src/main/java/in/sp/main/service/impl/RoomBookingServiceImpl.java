package in.sp.main.service.impl;

import in.sp.main.entity.RoomBooking;
import in.sp.main.repository.RoomBookingRepository;
import in.sp.main.service.RoomBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomBookingServiceImpl implements RoomBookingService {
    
    private final RoomBookingRepository roomBookingRepository;
    
    @Override
    public List<RoomBooking> findAll() {
        return roomBookingRepository.findAll();
    }
    
    @Override
    public Optional<RoomBooking> findById(Long id) {
        return roomBookingRepository.findById(id);
    }
    
    @Override
    public RoomBooking createBooking(RoomBooking booking) {
        booking.setStatus(RoomBooking.BookingStatus.CONFIRMED);
        return roomBookingRepository.save(booking);
    }
    
    @Override
    public RoomBooking updateBooking(RoomBooking booking) {
        return roomBookingRepository.save(booking);
    }
    
    @Override
    public void deleteBooking(Long id) {
        roomBookingRepository.deleteById(id);
    }
    
    @Override
    public List<RoomBooking> findByUserId(Long userId) {
        return roomBookingRepository.findByUserId(userId);
    }
    
    @Override
    public List<RoomBooking> findByRoomId(Long roomId) {
        return roomBookingRepository.findByRoomId(roomId);
    }
    
    @Override
    public List<RoomBooking> findByHotelId(Long hotelId) {
        return roomBookingRepository.findByHotelId(hotelId);
    }
    
    @Override
    public List<RoomBooking> findByStatus(RoomBooking.BookingStatus status) {
        return roomBookingRepository.findByStatus(status);
    }
    
    @Override
    public List<RoomBooking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate) {
        return roomBookingRepository.findByCheckInDateBetween(startDate, endDate);
    }
    
    @Override
    public List<RoomBooking> findByRoomIdAndDateRange(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return roomBookingRepository.findByRoomIdAndDateRange(roomId, checkIn, checkOut);
    }
} 