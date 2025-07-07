package in.sp.main.service.impl;

import in.sp.main.entity.TableBooking;
import in.sp.main.repository.TableBookingRepository;
import in.sp.main.service.TableBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TableBookingServiceImpl implements TableBookingService {
    
    private final TableBookingRepository tableBookingRepository;
    
    @Override
    public List<TableBooking> findAll() {
        return tableBookingRepository.findAll();
    }
    
    @Override
    public Optional<TableBooking> findById(Long id) {
        return tableBookingRepository.findById(id);
    }
    
    @Override
    public TableBooking createBooking(TableBooking booking) {
        booking.setStatus(TableBooking.BookingStatus.CONFIRMED);
        return tableBookingRepository.save(booking);
    }
    
    @Override
    public TableBooking updateBooking(TableBooking booking) {
        return tableBookingRepository.save(booking);
    }
    
    @Override
    public void deleteBooking(Long id) {
        tableBookingRepository.deleteById(id);
    }
    
    @Override
    public List<TableBooking> findByUserId(Long userId) {
        return tableBookingRepository.findByUserId(userId);
    }
    
    @Override
    public List<TableBooking> findByRestaurantId(Long restaurantId) {
        return tableBookingRepository.findByRestaurantId(restaurantId);
    }
    
    @Override
    public List<TableBooking> findByStatus(TableBooking.BookingStatus status) {
        return tableBookingRepository.findByStatus(status);
    }
    
    @Override
    public boolean checkAvailability(Long restaurantId, LocalDate date, LocalTime time, Integer guests) {
        // Simple availability check - in a real application, this would check against actual table capacity
        List<TableBooking> existingBookings = tableBookingRepository.findByRestaurantIdAndBookingDateAndBookingTime(restaurantId, date, time);
        int totalBookedGuests = existingBookings.stream()
                .mapToInt(TableBooking::getNumberOfGuests)
                .sum();
        
        // Assume restaurant has capacity for 50 guests at a time
        int restaurantCapacity = 50;
        return (totalBookedGuests + guests) <= restaurantCapacity;
    }
    
    @Override
    public List<TableBooking> findByRestaurantIdAndBookingDate(Long restaurantId, LocalDate bookingDate) {
        return tableBookingRepository.findByRestaurantIdAndBookingDate(restaurantId, bookingDate);
    }
    
    @Override
    public List<TableBooking> findByRestaurantIdAndBookingDateRange(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        return tableBookingRepository.findByRestaurantIdAndBookingDateBetween(restaurantId, startDate, endDate);
    }
} 