package backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pool_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private String studentId;
    private String pickupLocation;
    private String destination;
    private LocalDateTime expectedArrivalTime;
    private String status = "PENDING";

    // Stores the unique group ID assigned to matched students for the chat room
    private String groupId;
}