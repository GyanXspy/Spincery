package in.sp.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "food_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;
    
    @NotNull(message = "Delivery address is required")
    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;
    
    @Column(name = "delivery_instructions")
    private String deliveryInstructions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "subtotal")
    private Double subtotal;
    
    @Column(name = "delivery_charges")
    private Double deliveryCharges;
    
    @Column(name = "packaging_charges")
    private Double packagingCharges;
    
    @Column(name = "discount")
    private Double discount;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_status")
    private String paymentStatus;
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "feedback")
    private String feedback;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
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
    
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, ON_THE_WAY, DELIVERED, CANCELLED
    }
} 