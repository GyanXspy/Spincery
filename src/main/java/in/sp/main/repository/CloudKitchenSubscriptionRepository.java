package in.sp.main.repository;

import in.sp.main.entity.CloudKitchenSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudKitchenSubscriptionRepository extends JpaRepository<CloudKitchenSubscription, Long> {
    
    List<CloudKitchenSubscription> findByUserId(Long userId);
    
    @Query("SELECT cs FROM CloudKitchenSubscription cs WHERE cs.mealPlan.cloudKitchen.id = :cloudKitchenId")
    List<CloudKitchenSubscription> findByCloudKitchenId(@Param("cloudKitchenId") Long cloudKitchenId);
    
    List<CloudKitchenSubscription> findByStatus(CloudKitchenSubscription.SubscriptionStatus status);
    
    @Query("SELECT cs FROM CloudKitchenSubscription cs WHERE cs.user.id = :userId AND cs.status = :status")
    List<CloudKitchenSubscription> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CloudKitchenSubscription.SubscriptionStatus status);
} 