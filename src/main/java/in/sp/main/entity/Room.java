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
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", nullable = false)
    private String roomNumber;
    
    @NotBlank(message = "Room type is required")
    @Column(name = "room_type", nullable = false)
    private String roomType;
    
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Price per night is required")
    @Positive(message = "Price must be positive")
    @Column(name = "price_per_night", nullable = false)
    private Double pricePerNight;
    
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Column(name = "amenities")
    private String amenities;
    
    @Column(name = "room_photos")
    private String roomPhotos;
    
    @Column(name = "is_available")
    private boolean isAvailable = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus status = RoomStatus.AVAILABLE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
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
    
    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED
    }
} 