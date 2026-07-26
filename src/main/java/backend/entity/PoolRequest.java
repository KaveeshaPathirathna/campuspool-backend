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

    @Column(name = "destination")
    private String destination;

    private int totalSeats;
    private int filledSeats;
    private String note;
    private String genderRestriction; // "Female Only", "Any", etc.

    @Column(name = "request_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime time;

    // Added Student Relationship
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Default Constructor
    public PoolRequest() {
    }

    public PoolRequest(Long id, String pickupLocation, String destination, int totalSeats, int filledSeats, String note, String genderRestriction, LocalDateTime time, Student student) {
        this.id = id;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.filledSeats = filledSeats;
        this.note = note;
        this.genderRestriction = genderRestriction;
        this.time = time;
        this.student = student;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDropLocation() { return destination; }
    public void setDropLocation(String dropLocation) { this.destination = dropLocation; }

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

    // Getters and Setters for Student
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}