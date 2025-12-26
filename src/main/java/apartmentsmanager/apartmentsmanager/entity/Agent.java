package apartmentsmanager.apartmentsmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a real estate agent
 */
@Entity
@Table(name = "agents", uniqueConstraints = {
    @UniqueConstraint(columnNames = "license_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @NotBlank(message = "Лицензният номер е задължителен")
    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;
    
    @Column(columnDefinition = "TEXT")
    private String bio; // Biography
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(columnDefinition = "TEXT")
    private String specializations; // Comma-separated or JSON
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-Many relationship with Properties (apartments listed by this agent)
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties = new ArrayList<>();
    
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



