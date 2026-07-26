package backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pool_requests")
public class PoolRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pickupLocation;

    // Fix: Map the dropLocation field directly to the 'destination' column in MySQL
    @Column(name = "destination")
    private String dropLocation;

    private int totalSeats;
    private int filledSeats;
    private String note;
    private String genderRestriction; // "Female Only", "Any", etc.

    // Fix 1: Map 'time' field to 'request_time' column to avoid MySQL reserved keyword conflict
    // Fix 2: Convert ISO Date/Time string from Flutter into Java LocalDateTime
    @Column(name = "request_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime time;

    // Default Constructor (Required by JPA)
    public PoolRequest() {
    }

    public PoolRequest(Long id, String pickupLocation, String dropLocation, int totalSeats, int filledSeats, String note, String genderRestriction, LocalDateTime time) {
        this.id = id;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.totalSeats = totalSeats;
        this.filledSeats = filledSeats;
        this.note = note;
        this.genderRestriction = genderRestriction;
        this.time = time;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getFilledSeats() { return filledSeats; }
    public void setFilledSeats(int filledSeats) { this.filledSeats = filledSeats; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getGenderRestriction() { return genderRestriction; }
    public void setGenderRestriction(String genderRestriction) { this.genderRestriction = genderRestriction; }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}