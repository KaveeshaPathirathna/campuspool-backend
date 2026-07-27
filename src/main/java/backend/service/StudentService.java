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

    // 1. Student Registration
    public Student registerStudent(Student student) {
        // Prevent registering existing Student ID
        if (student.getStudentId() != null && studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalStateException("Student ID is already registered!");
        }

        // Prevent registering existing Campus Mail
        if (student.getCampusMail() != null && studentRepository.existsByCampusMail(student.getCampusMail())) {
            throw new IllegalStateException("Campus Mail is already registered!");
        }

        return studentRepository.save(student);
    }

    // 2. Student Login Method (Supports both Campus Mail & Student ID)
    public Student loginStudent(String identifier, String password) {
        // Step A: Search DB by Campus Mail first; if empty, search by Student ID
        Optional<Student> studentOpt = studentRepository.findByCampusMail(identifier);

        if (studentOpt.isEmpty()) {
            studentOpt = studentRepository.findByStudentId(identifier);
        }

        // Step B: Check if account exists (If deleted from DB, throws error here)
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found or account has been deleted!");
        }

        Student student = studentOpt.get();

        // Step C: Verify Password
        if (!student.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password!");
        }

        return student;
    }

    // 3. Get All Students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // 4. Delete Student by Student ID
    public void deleteStudentByStudentId(String studentId) {
        if (!studentRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist!");
        }
        studentRepository.deleteByStudentId(studentId);
    }
}