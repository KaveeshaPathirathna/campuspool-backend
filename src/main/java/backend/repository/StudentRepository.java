package backend.repository;

import backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    // JpaRepository<Student, String> එකෙන් Student කියන්නේ Entity එක, String කියන්නේ Primary Key එකේ type එක (studentId).
}
