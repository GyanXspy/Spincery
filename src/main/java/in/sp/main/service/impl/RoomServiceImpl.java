package in.sp.main.service.impl;

import in.sp.main.entity.Room;
import in.sp.main.repository.RoomRepository;
import in.sp.main.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    
    private final RoomRepository roomRepository;
    
    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }
    
    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }
    
    @Override
    public Room save(Room room) {
        return roomRepository.save(room);
    }
    
    @Override
    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }
    
    @Override
    public List<Room> findByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }
    
    @Override
    public List<Room> findByHotelIdAndIsAvailableTrue(Long hotelId) {
        return roomRepository.findByHotelIdAndIsAvailableTrue(hotelId);
    }
    
    @Override
    public List<Room> findByRoomType(String roomType) {
        return roomRepository.findByRoomType(roomType);
    }
    
    @Override
    public List<Room> findByHotelIdAndRoomType(Long hotelId, String roomType) {
        return roomRepository.findByHotelIdAndRoomType(hotelId, roomType);
    }
    
    @Override
    public List<Room> findByHotelIdAndPriceRange(Long hotelId, Double minPrice, Double maxPrice) {
        return roomRepository.findByHotelIdAndPriceRange(hotelId, minPrice, maxPrice);
    }
    
    @Override
    public List<Room> findByHotelIdAndCapacityGreaterThanEqual(Long hotelId, Integer capacity) {
        return roomRepository.findByHotelIdAndCapacityGreaterThanEqual(hotelId, capacity);
    }
    
    @Override
    public List<Room> findByHotelIdAndAvailableAndPriceLessThanEqual(Long hotelId, Double maxPrice) {
        return roomRepository.findByHotelIdAndAvailableAndPriceLessThanEqual(hotelId, maxPrice);
    }
} 