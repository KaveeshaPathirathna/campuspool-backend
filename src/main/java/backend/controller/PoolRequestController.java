package backend.controller;

import backend.entity.PoolRequest;
import backend.entity.Student;
import backend.repository.PoolRequestRepository;
import backend.repository.StudentRepository;
import backend.service.PoolRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class PoolRequestController {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    @Autowired
    private StudentRepository studentRepository; // 🔑 Inject StudentRepository

    @Autowired
    private PoolRequestService poolRequestService;

    // 🆕 Updated Create Request Endpoint (Fixes Student Linking)
    @PostMapping("/create")
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> payload) {
        try {
            // Extract studentId safely from payload
            String studentId = null;
            if (payload.containsKey("studentId") && payload.get("studentId") != null) {
                studentId = payload.get("studentId").toString().trim();
            } else if (payload.containsKey("student") && payload.get("student") instanceof Map) {
                Map<?, ?> studentMap = (Map<?, ?>) payload.get("student");
                if (studentMap.get("studentId") != null) {
                    studentId = studentMap.get("studentId").toString().trim();
                }
            }

            if (studentId == null || studentId.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: studentId is required to create a request!");
            }

            // Find student entity from DB
            Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
            if (!studentOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Error: Student with ID " + studentId + " not found!");
            }

            // Map JSON to PoolRequest Entity
            PoolRequest request = new PoolRequest();
            request.setPickupLocation((String) payload.get("pickupLocation"));

            String destination = payload.get("destination") != null ? (String) payload.get("destination") : (String) payload.get("dropLocation");
            request.setDestination(destination);

            request.setNote((String) payload.get("note"));
            request.setGenderRestriction((String) payload.get("genderRestriction"));

            if (payload.get("totalSeats") != null) {
                request.setTotalSeats(Integer.parseInt(payload.get("totalSeats").toString()));
            } else {
                request.setTotalSeats(3); // Default
            }

            request.setFilledSeats(1); // Creator takes 1 seat automatically
            request.setStudent(studentOpt.get()); // 🔑 KEY FIX: Link Student entity to PoolRequest

            PoolRequest savedRequest = poolRequestRepository.save(request);
            return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating request: " + e.getMessage());
        }
    }

    // 🔄 Get all Ride Requests
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRequests(@RequestParam(required = false) String studentId) {
        List<PoolRequest> requests = poolRequestService.getAllRequests();

        List<Map<String, Object>> responseList = requests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("pickupLocation", req.getPickupLocation());
            map.put("dropLocation", req.getDestination() != null ? req.getDestination() : req.getDropLocation());
            map.put("totalSeats", req.getTotalSeats());
            map.put("filledSeats", req.getFilledSeats());
            map.put("note", req.getNote());
            map.put("genderRestriction", req.getGenderRestriction());
            map.put("time", req.getTime());

            String reqOwnerId = "";
            if (req.getStudent() != null) {
                reqOwnerId = req.getStudent().getStudentId();
                map.put("studentId", reqOwnerId);
                map.put("student", req.getStudent());
            }

            // Check if current logged-in user is Owner OR Joined
            boolean isJoined = false;
            if (studentId != null && !studentId.trim().isEmpty()) {
                String cleanStudentId = studentId.trim().toLowerCase();
                if (!reqOwnerId.isEmpty() && cleanStudentId.equalsIgnoreCase(reqOwnerId.trim())) {
                    isJoined = true;
                }
            }

            map.put("isJoined", isJoined);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // 💡 Auto Matching / Suggestion Endpoint
    @GetMapping("/suggest")
    public ResponseEntity<List<PoolRequest>> getSuggestedPools(
            @RequestParam String pickup,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @RequestParam String studentId) {

        List<PoolRequest> suggestions = poolRequestService.getSuggestedPools(pickup, destination, time, studentId);
        return ResponseEntity.ok(suggestions);
    }

    // Get requests created by a specific user
    @GetMapping("/user/{studentId}")
    public ResponseEntity<List<PoolRequest>> getRequestsByUser(@PathVariable String studentId) {
        return ResponseEntity.ok(poolRequestRepository.findByStudent_StudentId(studentId));
    }

    // Update pool request details
    @PutMapping("/{requestId}")
    public ResponseEntity<?> updateRequest(@PathVariable Long requestId, @RequestBody PoolRequest requestDetails) {
        return poolRequestRepository.findById(requestId).map(req -> {
            if (requestDetails.getDestination() != null) {
                req.setDestination(requestDetails.getDestination());
            }
            PoolRequest updated = poolRequestRepository.save(req);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete pool request by ID
    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long requestId) {
        if (poolRequestRepository.existsById(requestId)) {
            poolRequestRepository.deleteById(requestId);
            return ResponseEntity.ok("Deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Join Ride Request
    @PutMapping("/{id}/join")
    public ResponseEntity<?> joinRequest(@PathVariable Long id, @RequestParam(required = false) String studentId) {
        try {
            Optional<PoolRequest> optionalRequest = poolRequestRepository.findById(id);
            if (optionalRequest.isPresent()) {
                PoolRequest request = optionalRequest.get();

                if (studentId != null && request.getStudent() != null &&
                        studentId.trim().equalsIgnoreCase(request.getStudent().getStudentId().trim())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("You cannot join your own ride request!");
                }

                if (request.getFilledSeats() < request.getTotalSeats()) {
                    request.setFilledSeats(request.getFilledSeats() + 1);
                    PoolRequest updatedRequest = poolRequestRepository.save(request);
                    return ResponseEntity.ok(updatedRequest);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("This ride request is already full!");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Ride request not found!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error joining request: " + e.getMessage());
        }
    }

    // Leave Ride Request
    @PutMapping("/{id}/leave")
    public ResponseEntity<?> leaveRequest(@PathVariable Long id, @RequestParam(required = false) String studentId) {
        try {
            Optional<PoolRequest> optionalRequest = poolRequestRepository.findById(id);
            if (optionalRequest.isPresent()) {
                PoolRequest request = optionalRequest.get();

                if (request.getFilledSeats() > 0) {
                    request.setFilledSeats(request.getFilledSeats() - 1);
                    PoolRequest updatedRequest = poolRequestRepository.save(request);
                    return ResponseEntity.ok(updatedRequest);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Cannot leave: Filled seats count is already 0!");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Ride request not found!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error leaving request: " + e.getMessage());
        }
    }
}