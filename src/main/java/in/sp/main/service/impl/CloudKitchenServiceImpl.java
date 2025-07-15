package in.sp.main.service.impl;

import in.sp.main.entity.CloudKitchen;
import in.sp.main.repository.CloudKitchenRepository;
import in.sp.main.service.CloudKitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CloudKitchenServiceImpl implements CloudKitchenService {
    
    private final CloudKitchenRepository cloudKitchenRepository;
    
    @Override
    public List<CloudKitchen> findAll() {
        return cloudKitchenRepository.findAll();
    }
    
    @Override
    public Optional<CloudKitchen> findById(Long id) {
        return cloudKitchenRepository.findById(id);
    }
    
    @Override
    public CloudKitchen save(CloudKitchen cloudKitchen) {
        return cloudKitchenRepository.save(cloudKitchen);
    }
    
    @Override
    public void deleteById(Long id) {
        cloudKitchenRepository.deleteById(id);
    }
    
    @Override
    public List<CloudKitchen> findByCity(String city) {
        return cloudKitchenRepository.findByCity(city);
    }
    
    @Override
    public List<CloudKitchen> findByOwnerId(Long ownerId) {
        return cloudKitchenRepository.findByOwnerId(ownerId);
    }
    
    @Override
    public List<CloudKitchen> findActiveVerifiedByCity(String city) {
        return cloudKitchenRepository.findActiveVerifiedByCity(city);
    }
    
    @Override
    public List<CloudKitchen> findByKitchenNameContaining(String name) {
        return cloudKitchenRepository.findByKitchenNameContaining(name);
    }

    @Override
    public List<CloudKitchen> findByIsVerified(boolean isVerified) {
        return cloudKitchenRepository.findByIsVerified(isVerified);
    }

    @Override
    public List<CloudKitchen> findByIsVerifiedTrue() {
        return cloudKitchenRepository.findByIsVerifiedTrue();
    }

    @Override
    public void verifyCloudKitchen(Long kitchenId) {
        CloudKitchen kitchen = cloudKitchenRepository.findById(kitchenId).orElseThrow(() -> new RuntimeException("Cloud Kitchen not found"));
        kitchen.setIsVerified(true);
        cloudKitchenRepository.save(kitchen);
    }

    @Override
    public void rejectCloudKitchen(Long kitchenId) {
        CloudKitchen kitchen = cloudKitchenRepository.findById(kitchenId).orElseThrow(() -> new RuntimeException("Cloud Kitchen not found"));
        cloudKitchenRepository.delete(kitchen);
    }
} 