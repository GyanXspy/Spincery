package in.sp.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Plan name is required")
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false)
    private DurationType durationType;
    
    @Column(name = "duration_value")
    private Integer durationValue;
    
    @Column(name = "breakfast_menu")
    private String breakfastMenu;
    
    @Column(name = "lunch_menu")
    private String lunchMenu;
    
    @Column(name = "dinner_menu")
    private String dinnerMenu;
    
    @Column(name = "dietary_preferences")
    private String dietaryPreferences;
    
    @Column(name = "is_available")
    private boolean isAvailable = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloud_kitchen_id", nullable = false)
    private CloudKitchen cloudKitchen;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DurationType {
        DAILY, WEEKLY, MONTHLY
    }
} 