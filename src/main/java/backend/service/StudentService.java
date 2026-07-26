package backend.service;

import backend.entity.Student;
import backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // Student Registration
    public Student registerStudent(Student student) {
        return studentRepository.save(student);
    }

    // 🔑 Student Login Method (This fixes your error!)
    public Student loginStudent(String email, String password) {
        Optional<Student> studentOpt = studentRepository.findByCampusMail(email);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            // Check password match
            if (student.getPassword().equals(password)) {
                return student;
            }
        }

        // Throws exception if email not found or password incorrect
        throw new IllegalArgumentException("Invalid email or password");
    }

    // Get All Students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}