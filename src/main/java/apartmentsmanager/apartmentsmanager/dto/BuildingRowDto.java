package apartmentsmanager.apartmentsmanager.dto;

import java.time.LocalDateTime;

/**
 * DTO за ред в списъка със сгради. Използва се вместо JPA entity в view,
 * за да се избегне LazyInitializationException и прекъснат отговор (ERR_INCOMPLETE_CHUNKED_ENCODING).
 */
public class BuildingRowDto {
    private Long id;
    private String name;
    private String address;
    private String status;
    private String stage;
    private LocalDateTime createdAt;
    private int apartmentsCount;
    private int garagesCount;
    private int basementsCount;
    private int parkingSpacesCount;
    private int commercialSpacesCount;
    private int totalCount;

    public BuildingRowDto() {}

    public BuildingRowDto(Long id, String name, String address, String status, String stage, LocalDateTime createdAt,
                          int apartmentsCount, int garagesCount, int basementsCount,
                          int parkingSpacesCount, int commercialSpacesCount, int totalCount) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.stage = stage;
        this.createdAt = createdAt;
        this.apartmentsCount = apartmentsCount;
        this.garagesCount = garagesCount;
        this.basementsCount = basementsCount;
        this.parkingSpacesCount = parkingSpacesCount;
        this.commercialSpacesCount = commercialSpacesCount;
        this.totalCount = totalCount;
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getApartmentsCount() { return apartmentsCount; }
    public void setApartmentsCount(int apartmentsCount) { this.apartmentsCount = apartmentsCount; }
    public int getGaragesCount() { return garagesCount; }
    public void setGaragesCount(int garagesCount) { this.garagesCount = garagesCount; }
    public int getBasementsCount() { return basementsCount; }
    public void setBasementsCount(int basementsCount) { this.basementsCount = basementsCount; }
    public int getParkingSpacesCount() { return parkingSpacesCount; }
    public void setParkingSpacesCount(int parkingSpacesCount) { this.parkingSpacesCount = parkingSpacesCount; }
    public int getCommercialSpacesCount() { return commercialSpacesCount; }
    public void setCommercialSpacesCount(int commercialSpacesCount) { this.commercialSpacesCount = commercialSpacesCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
