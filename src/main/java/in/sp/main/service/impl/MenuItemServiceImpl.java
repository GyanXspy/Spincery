package in.sp.main.service.impl;

import in.sp.main.entity.MenuItem;
import in.sp.main.repository.MenuItemRepository;
import in.sp.main.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {
    
    private final MenuItemRepository menuItemRepository;
    
    @Override
    public List<MenuItem> findAll() {
        return menuItemRepository.findAll();
    }
    
    @Override
    public Optional<MenuItem> findById(Long id) {
        return menuItemRepository.findById(id);
    }
    
    @Override
    public MenuItem save(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }
    
    @Override
    public void deleteById(Long id) {
        menuItemRepository.deleteById(id);
    }
    
    @Override
    public List<MenuItem> findByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }
    
    @Override
    public List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }
    
    @Override
    public List<MenuItem> findByCategory(String category) {
        return menuItemRepository.findByCategory(category);
    }
    
    @Override
    public List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category) {
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category);
    }
} 