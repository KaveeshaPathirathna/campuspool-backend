package backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friends")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_student_id", nullable = false)
    private String userStudentId; // Main user's student ID

    @Column(name = "friend_student_id", nullable = false)
    private String friendStudentId; // Friend's student ID

    @Column(nullable = false)
    private String status = "ACCEPTED"; // PENDING, ACCEPTED
}