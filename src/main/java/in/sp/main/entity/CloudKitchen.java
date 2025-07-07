package in.sp.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cloud_kitchens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudKitchen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kitchen name is required")
    @Column(name = "kitchen_name", nullable = false)
    private String kitchenName;
    
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
    
    @Column(name = "kitchen_logo_url")
    private String kitchenLogoUrl;
    
    @Column(name = "cover_photo_url")
    private String coverPhotoUrl;
    
    @Column(name = "kitchen_photos")
    private String kitchenPhotos;
    
    @Column(name = "delivery_radius")
    private Double deliveryRadius;
    
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
    
    @OneToMany(mappedBy = "cloudKitchen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MealPlan> mealPlans;
    
    @OneToMany(mappedBy = "cloudKitchen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CloudKitchenSubscription> subscriptions;
    
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