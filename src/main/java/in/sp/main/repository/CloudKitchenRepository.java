package in.sp.main.repository;

import in.sp.main.entity.CloudKitchen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudKitchenRepository extends JpaRepository<CloudKitchen, Long> {
    
    List<CloudKitchen> findByCity(String city);
    
    List<CloudKitchen> findByCityAndIsActiveTrue(String city);
    
    List<CloudKitchen> findByIsVerifiedTrue();
    
    List<CloudKitchen> findByIsActiveTrue();
    
    @Query("SELECT ck FROM CloudKitchen ck WHERE ck.city = :city AND ck.isActive = true AND ck.isVerified = true")
    List<CloudKitchen> findActiveVerifiedByCity(@Param("city") String city);
    
    @Query("SELECT ck FROM CloudKitchen ck WHERE ck.owner.id = :ownerId")
    List<CloudKitchen> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT ck FROM CloudKitchen ck WHERE ck.kitchenName LIKE %:name% AND ck.isActive = true")
    List<CloudKitchen> findByKitchenNameContaining(@Param("name") String name);
} 