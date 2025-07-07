package in.sp.main.service.impl;

import in.sp.main.entity.FoodOrder;
import in.sp.main.repository.FoodOrderRepository;
import in.sp.main.service.FoodOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodOrderServiceImpl implements FoodOrderService {
    
    private final FoodOrderRepository foodOrderRepository;
    
    @Override
    public List<FoodOrder> findAll() {
        return foodOrderRepository.findAll();
    }
    
    @Override
    public Optional<FoodOrder> findById(Long id) {
        return foodOrderRepository.findById(id);
    }
    
    @Override
    public FoodOrder createOrder(FoodOrder order) {
        order.setStatus(FoodOrder.OrderStatus.PENDING);
        return foodOrderRepository.save(order);
    }
    
    @Override
    public FoodOrder updateOrder(FoodOrder order) {
        return foodOrderRepository.save(order);
    }
    
    @Override
    public void deleteOrder(Long id) {
        foodOrderRepository.deleteById(id);
    }
    
    @Override
    public List<FoodOrder> findByUserId(Long userId) {
        return foodOrderRepository.findByUserId(userId);
    }
    
    @Override
    public List<FoodOrder> findByRestaurantId(Long restaurantId) {
        return foodOrderRepository.findByRestaurantId(restaurantId);
    }
    
    @Override
    public List<FoodOrder> findByStatus(FoodOrder.OrderStatus status) {
        return foodOrderRepository.findByStatus(status);
    }
    
    @Override
    public List<FoodOrder> findByUserIdAndStatus(Long userId, FoodOrder.OrderStatus status) {
        return foodOrderRepository.findByUserIdAndStatus(userId, status);
    }
} 