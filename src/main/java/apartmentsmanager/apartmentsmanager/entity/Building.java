package apartmentsmanager.apartmentsmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a building
 */
@Entity
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Building {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Името на сградата е задължително")
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 100)
    private String status; // активна, в разработка, завършена, на пауза
    
    @Column(name = "stage", length = 100)
    private String stage; // Етап: Открита строителна площадка, Акт 14, Акт 15, Акт 16, Завършен
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-Many relationship with Apartments
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Apartment> apartments;
    
    // One-to-Many relationship with Garages
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Garage> garages;
    
    // One-to-Many relationship with Basements
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Basement> basements;
    
    // One-to-Many relationship with Parking Spaces
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ParkingSpace> parkingSpaces;
    
    // One-to-Many relationship with Commercial Spaces
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CommercialSpace> commercialSpaces;
    
    // Getter methods that ensure lists are initialized
    public List<Apartment> getApartments() {
        if (apartments == null) {
            apartments = new ArrayList<>();
        }
        return apartments;
    }
    
    public List<Garage> getGarages() {
        if (garages == null) {
            garages = new ArrayList<>();
        }
        return garages;
    }
    
    public List<Basement> getBasements() {
        if (basements == null) {
            basements = new ArrayList<>();
        }
        return basements;
    }
    
    public List<ParkingSpace> getParkingSpaces() {
        if (parkingSpaces == null) {
            parkingSpaces = new ArrayList<>();
        }
        return parkingSpaces;
    }
    
    public List<CommercialSpace> getCommercialSpaces() {
        if (commercialSpaces == null) {
            commercialSpaces = new ArrayList<>();
        }
        return commercialSpaces;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) {
            status = "активна";
        }
        // Initialize lists - use getters to ensure initialization
        getApartments();
        getGarages();
        getBasements();
        getParkingSpaces();
        getCommercialSpaces();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

