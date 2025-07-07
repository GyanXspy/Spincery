package in.sp.main.repository;

import in.sp.main.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    
    List<MealPlan> findByCloudKitchenId(Long cloudKitchenId);
    
    @Query("SELECT mp FROM MealPlan mp WHERE mp.cloudKitchen.id = :cloudKitchenId AND mp.isAvailable = true")
    List<MealPlan> findByCloudKitchenIdAndIsAvailableTrue(@Param("cloudKitchenId") Long cloudKitchenId);
    
    @Query("SELECT mp FROM MealPlan mp WHERE mp.price BETWEEN :minPrice AND :maxPrice")
    List<MealPlan> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    List<MealPlan> findByDurationType(MealPlan.DurationType durationType);
    
    @Query("SELECT mp FROM MealPlan mp WHERE mp.cloudKitchen.id = :cloudKitchenId AND mp.durationType = :durationType")
    List<MealPlan> findByCloudKitchenIdAndDurationType(@Param("cloudKitchenId") Long cloudKitchenId, @Param("durationType") MealPlan.DurationType durationType);
} 