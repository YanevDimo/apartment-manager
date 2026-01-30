package apartmentsmanager.apartmentsmanager.dto;

import java.time.LocalDateTime;

/**
 * DTO за текуща сграда в страницата със сгради. Използва се вместо JPA entity в view,
 * за да се избегне LazyInitializationException и прекъснат отговор.
 */
public class BuildingDetailDto {
    private Long id;
    private String name;
    private String address;
    private String status;
    private String stage;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BuildingDetailDto() {}

    public BuildingDetailDto(Long id, String name, String address, String status, String stage,
                             String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.stage = stage;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
