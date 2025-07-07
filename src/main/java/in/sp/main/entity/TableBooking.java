package in.sp.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "table_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableBooking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Booking date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;
    
    @NotNull(message = "Booking time is required")
    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;
    
    @NotNull(message = "Number of guests is required")
    @Positive(message = "Number of guests must be positive")
    @Column(name = "number_of_guests", nullable = false)
    private Integer numberOfGuests;
    
    @Column(name = "special_requests")
    private String specialRequests;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    @Column(name = "advance_payment")
    private Double advancePayment;
    
    @Column(name = "payment_status")
    private String paymentStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
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
    
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
} 