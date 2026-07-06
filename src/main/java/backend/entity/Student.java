package backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "campus_mail", nullable = false, unique = true, length = 100)
    private String campusMail;

    @Column(name = "university_name", nullable = false, length = 100)
    private String universityName;

    @Column(nullable = false, length = 100)
    private String faculty;

    @Column(name = "academic_year", nullable = false)
    private int academicYear;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;
}