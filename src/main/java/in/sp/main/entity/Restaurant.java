package in.sp.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Restaurant name is required")
    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;
    
    @NotBlank(message = "Owner name is required")
    @Column(name = "owner_name", nullable = false)
    private String ownerName;
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false)
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Column(name = "phone", nullable = false)
    private String phone;
    
    @Column(name = "whatsapp_number")
    private String whatsappNumber;
    
    @Column(name = "alternate_contact")
    private String alternateContact;
    
    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;
    
    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false)
    private String address;
    
    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false)
    private String city;
    
    @NotBlank(message = "State is required")
    @Column(name = "state", nullable = false)
    private String state;
    
    @NotBlank(message = "Zip code is required")
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
    
    @Column(name = "nearby_landmark")
    private String nearbyLandmark;
    
    @Column(name = "google_maps_link")
    private String googleMapsLink;
    
    @Column(name = "opening_time")
    private LocalTime openingTime;
    
    @Column(name = "closing_time")
    private LocalTime closingTime;
    
    @Column(name = "working_days")
    private String workingDays;
    
    @Column(name = "weekly_off")
    private String weeklyOff;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "cover_photo_url")
    private String coverPhotoUrl;
    
    @Column(name = "popular_dishes_photos")
    private String popularDishesPhotos;
    
    @Column(name = "menu_images")
    private String menuImages;
    
    @Column(name = "delivery_offered")
    private boolean deliveryOffered = false;
    
    @Column(name = "delivery_radius")
    private Double deliveryRadius;
    
    @Column(name = "delivery_mode")
    private String deliveryMode;
    
    @Column(name = "packaging_charges")
    private Double packagingCharges;
    
    @Column(name = "avg_preparation_time")
    private Integer avgPreparationTime;
    
    @Column(name = "accepted_payment_methods")
    private String acceptedPaymentMethods;
    
    @Column(name = "bank_account_holder")
    private String bankAccountHolder;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "ifsc_code")
    private String ifscCode;
    
    @Column(name = "upi_id")
    private String upiId;
    
    @Column(name = "fssai_license_number")
    private String fssaiLicenseNumber;
    
    @Column(name = "gstin")
    private String gstin;
    
    @Column(name = "fssai_certificate_url")
    private String fssaiCertificateUrl;
    
    @Column(name = "gst_certificate_url")
    private String gstCertificateUrl;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "cuisine")
    private String cuisine;
    
    @Column(name = "offers_discounts")
    private String offersDiscounts;
    
    @Column(name = "referral_code")
    private String referralCode;
    
    @Column(name = "social_media_links")
    private String socialMediaLinks;
    
    @Column(name = "rating")
    private Double rating = 0.0;
    
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Column(name = "is_verified")
    private boolean isVerified = false;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TableBooking> tableBookings;
    
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
} 