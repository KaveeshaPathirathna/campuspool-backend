package backend.service;

import backend.entity.Student;
import backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // 1. Register a new student (Save Student)
    public Student registerStudent(Student student) {
        // Check the campus email domain validation rule here
        if (!student.getCampusMail().endsWith(".ac.lk")) {
            throw new IllegalArgumentException("Invalid email address! Please use a valid campus email (.ac.lk).");
        }
        return studentRepository.save(student);
    }

    // 2. Get the list of all students (Get All Students)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}