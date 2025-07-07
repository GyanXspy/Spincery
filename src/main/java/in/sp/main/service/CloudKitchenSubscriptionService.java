package in.sp.main.service;

import in.sp.main.entity.CloudKitchenSubscription;

import java.util.List;
import java.util.Optional;

public interface CloudKitchenSubscriptionService {
    
    List<CloudKitchenSubscription> findAll();
    
    Optional<CloudKitchenSubscription> findById(Long id);
    
    CloudKitchenSubscription createSubscription(CloudKitchenSubscription subscription);
    
    CloudKitchenSubscription updateSubscription(CloudKitchenSubscription subscription);
    
    void deleteSubscription(Long id);
    
    List<CloudKitchenSubscription> findByUserId(Long userId);
    
    List<CloudKitchenSubscription> findByCloudKitchenId(Long cloudKitchenId);
    
    List<CloudKitchenSubscription> findByStatus(CloudKitchenSubscription.SubscriptionStatus status);
} 