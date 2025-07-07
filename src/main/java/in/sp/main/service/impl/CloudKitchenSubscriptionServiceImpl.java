package in.sp.main.service.impl;

import in.sp.main.entity.CloudKitchenSubscription;
import in.sp.main.repository.CloudKitchenSubscriptionRepository;
import in.sp.main.service.CloudKitchenSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CloudKitchenSubscriptionServiceImpl implements CloudKitchenSubscriptionService {
    
    private final CloudKitchenSubscriptionRepository subscriptionRepository;
    
    @Override
    public List<CloudKitchenSubscription> findAll() {
        return subscriptionRepository.findAll();
    }
    
    @Override
    public Optional<CloudKitchenSubscription> findById(Long id) {
        return subscriptionRepository.findById(id);
    }
    
    @Override
    public CloudKitchenSubscription createSubscription(CloudKitchenSubscription subscription) {
        subscription.setStatus(CloudKitchenSubscription.SubscriptionStatus.ACTIVE);
        return subscriptionRepository.save(subscription);
    }
    
    @Override
    public CloudKitchenSubscription updateSubscription(CloudKitchenSubscription subscription) {
        return subscriptionRepository.save(subscription);
    }
    
    @Override
    public void deleteSubscription(Long id) {
        subscriptionRepository.deleteById(id);
    }
    
    @Override
    public List<CloudKitchenSubscription> findByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
    
    @Override
    public List<CloudKitchenSubscription> findByCloudKitchenId(Long cloudKitchenId) {
        return subscriptionRepository.findByCloudKitchenId(cloudKitchenId);
    }
    
    @Override
    public List<CloudKitchenSubscription> findByStatus(CloudKitchenSubscription.SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status);
    }
} 