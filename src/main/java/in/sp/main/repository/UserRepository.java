package in.sp.main.repository;

import in.sp.main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.isVerified = :verified")
    List<User> findByVerificationStatus(@Param("verified") boolean verified);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isVerified = true")
    Optional<User> findByEmailAndVerified(@Param("email") String email);
} 