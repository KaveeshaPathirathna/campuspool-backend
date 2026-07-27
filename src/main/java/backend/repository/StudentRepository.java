package backend.repository;

import backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    // Find student by campus Mail for Login validation
    Optional<Student> findByCampusMail(String campusMail);

    // Find student by custom String student ID (e.g., "AS20220115")
    Optional<Student> findByStudentId(String studentId);

    // Check if student exists in database
    boolean existsByStudentId(String studentId);

    boolean existsByCampusMail(String campusMail);

    // Delete student directly using custom String studentId
    @Transactional
    void deleteByStudentId(String studentId);
}