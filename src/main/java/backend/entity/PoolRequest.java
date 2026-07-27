package backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pool_requests")
public class PoolRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student; // Creator of the pool

    private String pickupLocation;
    private String destination;
    private String dropLocation;
    private String note;
    private String genderRestriction;
    private int totalSeats = 3;
    private int maxSeats = 3;
    private int filledSeats = 1;

    // 🎯 Mapping 'time' to DB column 'request_time' & handling JSON 'requestTime'
    @Column(name = "request_time")
    @JsonProperty("requestTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime time;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 👥 Joined Students List
    // 🎯 Cascade settings updated to handle safe entity manipulation
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "pool_request_joined_students",
            joinColumns = @JoinColumn(name = "pool_request_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Student> joinedStudents = new ArrayList<>();

    // Default Constructor
    public PoolRequest() {}

    // Constructor with parameters
    public PoolRequest(Student student, String pickupLocation, String destination, LocalDateTime time, int maxSeats) {
        this.student = student;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.time = time;
        this.maxSeats = maxSeats;
        this.totalSeats = maxSeats;
        this.createdAt = LocalDateTime.now();
    }

    // 🎯 FIX: Automatically clear joined students relation before JPA deletes this entity
    @PreRemove
    private void removeJoinedStudentsOnDelete() {
        if (this.joinedStudents != null) {
            this.joinedStudents.clear();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDropLocation() {
        return dropLocation != null ? dropLocation : destination;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getGenderRestriction() {
        return genderRestriction;
    }

    public void setGenderRestriction(String genderRestriction) {
        this.genderRestriction = genderRestriction;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
        this.maxSeats = totalSeats;
    }

    public int getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(int maxSeats) {
        this.maxSeats = maxSeats;
        this.totalSeats = maxSeats;
    }

    // Dynamically calculate filled seats accurately
    public int getFilledSeats() {
        return getAllMembers().size();
    }

    public void setFilledSeats(int filledSeats) {
        this.filledSeats = filledSeats;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    // 🎯 Ignored in Jackson to avoid conflicting property exception
    @JsonIgnore
    public LocalDateTime getRequestTime() {
        return time;
    }

    @JsonIgnore
    public void setRequestTime(LocalDateTime requestTime) {
        this.time = requestTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Student> getJoinedStudents() {
        return joinedStudents;
    }

    public void setJoinedStudents(List<Student> joinedStudents) {
        this.joinedStudents = joinedStudents;
    }

    // Helper: Safe ID Comparison for Student Objects
    private boolean isStudentInList(List<Student> list, Student target) {
        if (list == null || target == null || target.getStudentId() == null) return false;
        return list.stream().anyMatch(s -> s.getStudentId() != null &&
                s.getStudentId().equalsIgnoreCase(target.getStudentId()));
    }

    // 🤝 Helper methods for joining/leaving group
    public void addMember(Student student) {
        if (student == null) return;
        if (this.joinedStudents == null) {
            this.joinedStudents = new ArrayList<>();
        }
        if (!isStudentInList(this.joinedStudents, student)) {
            this.joinedStudents.add(student);
        }
        this.filledSeats = getFilledSeats();
    }

    public void removeMember(Student student) {
        if (this.joinedStudents != null && student != null) {
            this.joinedStudents.removeIf(s -> s.getStudentId() != null &&
                    s.getStudentId().equalsIgnoreCase(student.getStudentId()));
        }
        this.filledSeats = getFilledSeats();
    }

    public int getOccupiedSeats() {
        return getFilledSeats();
    }

    // Returns all members (Creator + Joined Members) without duplicates
    public List<Student> getAllMembers() {
        List<Student> allMembers = new ArrayList<>();
        if (this.student != null) {
            allMembers.add(this.student);
        }
        if (this.joinedStudents != null) {
            for (Student s : this.joinedStudents) {
                if (!isStudentInList(allMembers, s)) {
                    allMembers.add(s);
                }
            }
        }
        return allMembers;
    }
}