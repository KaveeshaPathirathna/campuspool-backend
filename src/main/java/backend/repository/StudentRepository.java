package backend.repository;

import backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Find student by campus Mail for Login validation
    Optional<Student> findByCampusMail(String campusMail);

    // Find student by custom String student ID (e.g., "AS20220115")
    Optional<Student> findByStudentId(String studentId);
}