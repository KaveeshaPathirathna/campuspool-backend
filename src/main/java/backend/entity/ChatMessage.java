package backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Sender of the message (Relationship with Student entity)
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student sender;

    // Target pool group (Relationship with PoolRequest entity)
    @ManyToOne
    @JoinColumn(name = "pool_request_id", nullable = false)
    @JsonIgnoreProperties({"joinedStudents", "student", "hibernateLazyInitializer", "handler"})
    private PoolRequest poolRequest;

    // Automatically set the current timestamp before persisting to database
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}