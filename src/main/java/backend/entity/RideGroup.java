package backend.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "ride_groups")
public class RideGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pickupLocation;
    private String dropLocation;
    private LocalTime time;
    private String status; // "ACTIVE" or "COMPLETED"

    public RideGroup() {}

    public RideGroup(String pickupLocation, String dropLocation, LocalTime time, String status) {
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.time = time;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}