package backend.controller;

import backend.entity.Student;
import backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*") // Prevents Flutter Web CORS blocking
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 1. Student Registration
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody Student student) {
        try {
            Student savedStudent = studentService.registerStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed due to a server error.");
        }
    }

    // 2. Student Login
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("campusMail");
            String password = loginData.get("password");

            Student student = studentService.loginStudent(email, password);
            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed due to a server error.");
        }
    }

    // 3. Get All Students
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    // 4. ✅ Get All Students Except Logged-in User (For Friend Suggestions)
    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudentsExceptCurrent(@RequestParam(required = false) String currentStudentId) {
        try {
            List<Student> students = studentService.getAllStudents();
            if (currentStudentId != null && !currentStudentId.isEmpty()) {
                // Logged in user ge name eka suggestion list eken filter karala ayn karanawa
                students.removeIf(s -> currentStudentId.equals(s.getStudentId()));
            }
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}