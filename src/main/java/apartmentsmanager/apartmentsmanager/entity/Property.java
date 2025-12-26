package apartmentsmanager.apartmentsmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a property listing (extends Apartment concept for public listings)
 */
@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Заглавието е задължително")
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "Градът е задължителен")
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(length = 255)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PropertyType type;
    
    @NotNull(message = "Цената е задължителна")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(name = "price_range_min", precision = 15, scale = 2)
    private BigDecimal priceRangeMin;
    
    @Column(name = "price_range_max", precision = 15, scale = 2)
    private BigDecimal priceRangeMax;
    
    @Column(name = "bedrooms")
    private Integer bedrooms;
    
    @Column(name = "bathrooms")
    private Integer bathrooms;
    
    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area; // Area in square meters
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;
    
    @Column(nullable = false)
    private Boolean published = false; // Whether property is visible to public
    
    @Column(nullable = false)
    private Boolean featured = false; // Featured properties shown first
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-Many relationship with PropertyImages
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyImage> images = new ArrayList<>();
    
    // One-to-Many relationship with Inquiries
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inquiry> inquiries = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PropertyType {
        APARTMENT,      // Апартамент
        HOUSE,          // Къща
        OFFICE,         // Офис
        COMMERCIAL,     // Търговски обект
        LAND,           // Парцел
        VILLA,          // Вила
        STUDIO          // Студио
    }
}



