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
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Dish name is required")
    @Column(name = "dish_name", nullable = false)
    private String dishName;
    
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Column(name = "category")
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "food_type")
    private FoodType foodType = FoodType.VEG;
    
    @Column(name = "tags")
    private String tags;
    
    @Column(name = "is_available")
    private boolean available = true;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
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
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public enum FoodType {
        VEG, NON_VEG, EGG
    }
} 