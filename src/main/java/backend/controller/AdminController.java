package backend.controller;

import backend.entity.PoolRequest;
import backend.entity.Student;
import backend.repository.PoolRequestRepository;
import backend.repository.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final StudentRepository studentRepository;
    private final PoolRequestRepository poolRequestRepository;

    public AdminController(StudentRepository studentRepository, PoolRequestRepository poolRequestRepository) {
        this.studentRepository = studentRepository;
        this.poolRequestRepository = poolRequestRepository;
    }

    // ADMIN LOGIN
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> response = new HashMap<>();

        if ("admin".equals(username) && "admin123".equals(password)) {
            response.put("success", true);
            response.put("message", "Login Successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid Username or Password!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // STUDENT MANAGEMENT
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PutMapping("/students/{id:.+}")
    public ResponseEntity<?> updateStudent(@PathVariable("id") String id, @RequestBody Student updatedStudent) {
        Optional<Student> studentOpt = studentRepository.findById(id);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setName(updatedStudent.getName());
            student.setGender(updatedStudent.getGender());
            return ResponseEntity.ok(studentRepository.save(student));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found!");
        }
    }

    @DeleteMapping("/students/{id:.+}")
    public ResponseEntity<String> deleteStudent(@PathVariable("id") String id) {
        if (!studentRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found!");
        }

        try {
            studentRepository.deleteById(id);
            return ResponseEntity.ok("Student deleted successfully!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete student! This student is linked to active ride requests or groups.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting student: " + e.getMessage());
        }
    }

    // RIDE GROUP MANAGEMENT
    @GetMapping("/groups")
    public List<PoolRequest> getAllGroups() {
        return poolRequestRepository.findAll();
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody PoolRequest updatedGroup) {
        Optional<PoolRequest> groupOpt = poolRequestRepository.findById(id);

        if (groupOpt.isPresent()) {
            PoolRequest group = groupOpt.get();
            group.setPickupLocation(updatedGroup.getPickupLocation());
            group.setDestination(updatedGroup.getDestination());
            group.setTotalSeats(updatedGroup.getTotalSeats());
            group.setNote(updatedGroup.getNote());
            return ResponseEntity.ok(poolRequestRepository.save(group));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found!");
        }
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long id) {
        try {
            if (!poolRequestRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found!");
            }
            poolRequestRepository.deleteById(id);
            return ResponseEntity.ok("Ride group deleted successfully!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete ride group due to linked dependencies.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting group: " + e.getMessage());
        }
    }
}