package backend.controller;

import backend.repository.PoolRequestRepository;
import backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalUsers = studentRepository.count();
        long activePools = poolRequestRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activePools", activePools);
        // Live users calculate (Active users estimation)
        stats.put("liveUsers", totalUsers > 0 ? (totalUsers / 2) + 1 : 0);

        return ResponseEntity.ok(stats);
    }
}