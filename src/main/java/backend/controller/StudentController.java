package backend.controller;

import backend.entity.Student;
import backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // Helper Method: Email එකක් ආවොත් Index No එක පමණක් වෙන් කර Capitalize කර ගැනීම
    private String cleanStudentId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) return "";
        String trimmed = rawId.trim();
        if (trimmed.contains("@")) {
            trimmed = trimmed.split("@")[0];
        }
        return trimmed.toUpperCase();
    }

    // 1. Student Registration
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody Student student) {
        try {
            if (student.getStudentId() != null) {
                student.setStudentId(cleanStudentId(student.getStudentId()));
            }
            Student savedStudent = studentService.registerStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed due to a server error.");
        }
    }

    // 2. Student Login (Handles both campusMail or studentId & Validates Safely)
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody Map<String, String> loginData) {
        try {
            String identifier = loginData.get("campusMail");
            if (identifier == null || identifier.trim().isEmpty()) {
                identifier = loginData.get("studentId");
            }
            if (identifier == null || identifier.trim().isEmpty()) {
                identifier = loginData.get("identifier");
            }

            String password = loginData.get("password");

            if (identifier == null || identifier.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Campus Mail / Student ID and Password are required!");
            }

            // Authenticate via StudentService
            Student student = studentService.loginStudent(identifier.trim(), password.trim());

            if (student == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not found or account has been deleted!");
            }

            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed due to a server error.");
        }
    }

    // 3. Get All Students
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    // 4. Get Single Student by ID / Email (Profile view)
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable String studentId) {
        try {
            String cleanId = cleanStudentId(studentId);
            List<Student> students = studentService.getAllStudents();
            Optional<Student> studentOpt = students.stream()
                    .filter(s -> cleanId.equalsIgnoreCase(cleanStudentId(s.getStudentId())) ||
                            (s.getCampusMail() != null && s.getCampusMail().equalsIgnoreCase(studentId.trim())))
                    .findFirst();

            if (studentOpt.isPresent()) {
                return ResponseEntity.ok(studentOpt.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching student profile");
        }
    }

    // 5. Get All Students Except Logged-in User (Case-Insensitive & Email-Safe Filter Fix)
    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudentsExceptCurrent(@RequestParam(required = false) String currentStudentId) {
        try {
            List<Student> students = studentService.getAllStudents();

            if (currentStudentId != null && !currentStudentId.trim().isEmpty()) {
                String cleanCurrentId = cleanStudentId(currentStudentId);

                // Safely filter out current user regardless of Case or Email format
                students = students.stream()
                        .filter(s -> s.getStudentId() != null &&
                                !cleanStudentId(s.getStudentId()).equals(cleanCurrentId))
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 6. Delete Student Endpoint
    @DeleteMapping("/{studentId}")
    public ResponseEntity<?> deleteStudent(@PathVariable String studentId) {
        try {
            String cleanId = cleanStudentId(studentId);
            studentService.deleteStudentByStudentId(cleanId);
            return ResponseEntity.ok("Student deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete student: " + e.getMessage());
        }
    }
}