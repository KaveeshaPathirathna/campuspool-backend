package backend.controller;

import backend.entity.PoolRequest;
import backend.entity.Student;
import backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ✅ 0. Admin Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Simple hardcoded check for demo (Username: admin | Password: admin123)
        if ("admin".equals(username) && "admin123".equals(password)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Admin Username or Password!");
    }

    // 1. Get Overall Stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // 2. Get All Students
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    // 3. Delete Student
    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<?> deleteStudent(@PathVariable String studentId) {
        boolean deleted = adminService.deleteStudent(studentId);
        if (deleted) {
            return ResponseEntity.ok("Student deleted successfully");
        }
        return ResponseEntity.badRequest().body("Student not found");
    }

    // 4. Get All Pool Requests
    @GetMapping("/pools")
    public ResponseEntity<List<PoolRequest>> getAllPoolRequests() {
        return ResponseEntity.ok(adminService.getAllPoolRequests());
    }

    // 5. Delete Pool Request / Group Ride
    @DeleteMapping("/pools/{id}")
    public ResponseEntity<?> deletePoolRequest(@PathVariable Long id) {
        boolean deleted = adminService.deletePoolRequest(id);
        if (deleted) {
            return ResponseEntity.ok("Pool request deleted successfully");
        }
        return ResponseEntity.badRequest().body("Pool request not found");
    }
}