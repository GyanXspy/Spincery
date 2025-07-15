package in.sp.main.service;

import in.sp.main.entity.CloudKitchen;

import java.util.List;
import java.util.Optional;

public interface CloudKitchenService {
    
    List<CloudKitchen> findAll();
    
    Optional<CloudKitchen> findById(Long id);
    
    CloudKitchen save(CloudKitchen cloudKitchen);
    
    void deleteById(Long id);
    
    List<CloudKitchen> findByCity(String city);
    
    List<CloudKitchen> findByOwnerId(Long ownerId);
    
    List<CloudKitchen> findActiveVerifiedByCity(String city);
    
    List<CloudKitchen> findByKitchenNameContaining(String name);
    
    List<CloudKitchen> findByIsVerified(boolean isVerified);
    void verifyCloudKitchen(Long kitchenId);
    void rejectCloudKitchen(Long kitchenId);
    List<CloudKitchen> findByIsVerifiedTrue();
} 