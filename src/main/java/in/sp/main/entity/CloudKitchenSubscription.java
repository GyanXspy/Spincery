package in.sp.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cloud_kitchen_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudKitchenSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "delivery_address")
    private String deliveryAddress;
    
    @Column(name = "delivery_time_slots")
    private String deliveryTimeSlots;
    
    @Column(name = "dietary_preferences")
    private String dietaryPreferences;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    @Column(name = "payment_status")
    private String paymentStatus;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "feedback")
    private String feedback;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloud_kitchen_id", nullable = false)
    private CloudKitchen cloudKitchen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;
    
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
    
    public enum SubscriptionStatus {
        ACTIVE, PAUSED, CANCELLED, COMPLETED
    }
} 