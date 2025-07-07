package in.sp.main.service.impl;

import in.sp.main.entity.OrderItem;
import in.sp.main.repository.OrderItemRepository;
import in.sp.main.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    
    @Override
    public List<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }
    
    @Override
    public Optional<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }
    
    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
    
    @Override
    public void deleteById(Long id) {
        orderItemRepository.deleteById(id);
    }
    
    @Override
    public List<OrderItem> findByFoodOrderId(Long foodOrderId) {
        return orderItemRepository.findByFoodOrderId(foodOrderId);
    }
    
    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<OrderItem> findByRestaurantId(Long restaurantId) {
        return orderItemRepository.findByRestaurantId(restaurantId);
    }
    
    @Override
    public List<OrderItem> findByUserId(Long userId) {
        return orderItemRepository.findByUserId(userId);
    }
    
    @Override
    public List<OrderItem> findByMenuItemId(Long menuItemId) {
        return orderItemRepository.findByMenuItemId(menuItemId);
    }
} 