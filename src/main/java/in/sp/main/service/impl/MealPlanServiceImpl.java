package in.sp.main.service.impl;

import in.sp.main.entity.MealPlan;
import in.sp.main.repository.MealPlanRepository;
import in.sp.main.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealPlanServiceImpl implements MealPlanService {
    
    private final MealPlanRepository mealPlanRepository;
    
    @Override
    public List<MealPlan> findAll() {
        return mealPlanRepository.findAll();
    }
    
    @Override
    public Optional<MealPlan> findById(Long id) {
        return mealPlanRepository.findById(id);
    }
    
    @Override
    public MealPlan save(MealPlan mealPlan) {
        return mealPlanRepository.save(mealPlan);
    }
    
    @Override
    public void deleteById(Long id) {
        mealPlanRepository.deleteById(id);
    }
    
    @Override
    public List<MealPlan> findByCloudKitchenId(Long cloudKitchenId) {
        return mealPlanRepository.findByCloudKitchenId(cloudKitchenId);
    }
    
    @Override
    public List<MealPlan> findByCloudKitchenIdAndIsAvailableTrue(Long cloudKitchenId) {
        return mealPlanRepository.findByCloudKitchenIdAndIsAvailableTrue(cloudKitchenId);
    }
    
    @Override
    public List<MealPlan> findByPriceRange(Double minPrice, Double maxPrice) {
        return mealPlanRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    @Override
    public List<MealPlan> findByDurationType(MealPlan.DurationType durationType) {
        return mealPlanRepository.findByDurationType(durationType);
    }
    
    @Override
    public List<MealPlan> findByCloudKitchenIdAndDurationType(Long cloudKitchenId, MealPlan.DurationType durationType) {
        return mealPlanRepository.findByCloudKitchenIdAndDurationType(cloudKitchenId, durationType);
    }
} 