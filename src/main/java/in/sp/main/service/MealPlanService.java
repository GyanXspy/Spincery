package in.sp.main.service;

import in.sp.main.entity.MealPlan;

import java.util.List;
import java.util.Optional;

public interface MealPlanService {
    
    List<MealPlan> findAll();
    
    Optional<MealPlan> findById(Long id);
    
    MealPlan save(MealPlan mealPlan);
    
    void deleteById(Long id);
    
    List<MealPlan> findByCloudKitchenId(Long cloudKitchenId);
    
    List<MealPlan> findByCloudKitchenIdAndIsAvailableTrue(Long cloudKitchenId);
    
    List<MealPlan> findByPriceRange(Double minPrice, Double maxPrice);
    
    List<MealPlan> findByDurationType(MealPlan.DurationType durationType);
    
    List<MealPlan> findByCloudKitchenIdAndDurationType(Long cloudKitchenId, MealPlan.DurationType durationType);
} 