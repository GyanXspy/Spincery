package in.sp.main.service.impl;

import in.sp.main.entity.Hotel;
import in.sp.main.repository.HotelRepository;
import in.sp.main.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    
    private final HotelRepository hotelRepository;
    
    @Override
    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }
    
    @Override
    public Optional<Hotel> findById(Long id) {
        return hotelRepository.findById(id);
    }
    
    @Override
    public Hotel save(Hotel hotel) {
        return hotelRepository.save(hotel);
    }
    
    @Override
    public void deleteById(Long id) {
        hotelRepository.deleteById(id);
    }
    
    @Override
    public List<Hotel> findByCity(String city) {
        return hotelRepository.findByCity(city);
    }
    
    @Override
    public List<Hotel> findByOwnerId(Long ownerId) {
        return hotelRepository.findByOwnerId(ownerId);
    }
    
    @Override
    public List<Hotel> findByRatingGreaterThan(Double rating) {
        return hotelRepository.findByRatingGreaterThan(rating);
    }
    
    @Override
    public List<Hotel> findActiveVerifiedByCity(String city) {
        return hotelRepository.findActiveVerifiedByCity(city);
    }
    
    @Override
    public List<Hotel> findByHotelNameContaining(String name) {
        return hotelRepository.findByHotelNameContaining(name);
    }
} 