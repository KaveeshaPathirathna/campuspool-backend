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
@RequestMapping({"/api/requests", "/api/pools"})
@CrossOrigin(origins = "*")
public class PoolRequestController {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PoolRequestService poolRequestService;

    // 🆕 Create Ride Request (Automatically adds Creator as Joined Member)
    @PostMapping("/create")
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> payload) {
        try {
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

            // Fallback: If studentId is an Email, extract Index Number
            Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
            if (!studentOpt.isPresent() && studentId.contains("@")) {
                String cleanIndex = studentId.split("@")[0].toUpperCase();
                studentOpt = studentRepository.findByStudentId(cleanIndex);
            }

            if (!studentOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Error: Student with ID " + studentId + " not found!");
            }

            Student creator = studentOpt.get();

            PoolRequest request = new PoolRequest();
            request.setPickupLocation((String) payload.get("pickupLocation"));

            String destination = payload.get("destination") != null ?
                    (String) payload.get("destination") : (String) payload.get("dropLocation");
            request.setDestination(destination);

            request.setNote((String) payload.get("note"));
            request.setGenderRestriction((String) payload.get("genderRestriction"));

            if (payload.get("totalSeats") != null) {
                request.setTotalSeats(Integer.parseInt(payload.get("totalSeats").toString()));
            } else {
                request.setTotalSeats(3); // Default seats
            }

            // 🎯 SAFE TIME PARSING FIX
            Object timeObj = payload.get("requestTime") != null ? payload.get("requestTime") : payload.get("time");
            if (timeObj != null && !timeObj.toString().trim().isEmpty()) {
                try {
                    String timeStr = timeObj.toString().trim();
                    if (timeStr.endsWith("Z")) {
                        timeStr = timeStr.substring(0, timeStr.length() - 1);
                    }
                    if (timeStr.contains(".")) {
                        timeStr = timeStr.substring(0, timeStr.indexOf("."));
                    }
                    request.setTime(LocalDateTime.parse(timeStr));
                } catch (Exception parseEx) {
                    System.err.println("Warning: Time parse failed (" + timeObj + "), using current time.");
                    request.setTime(LocalDateTime.now());
                }
            } else {
                request.setTime(LocalDateTime.now());
            }

            request.setStudent(creator); // Set Creator
            request.addMember(creator);  // Auto-add Creator to Joined Members List
            request.setFilledSeats(request.getJoinedStudents().size()); // 1 seat filled automatically

            PoolRequest savedRequest = poolRequestRepository.save(request);
            return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating request: " + e.getMessage());
        }
    }

    // 🔄 Get All Ride Requests
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRequests(@RequestParam(required = false) String studentId) {
        List<PoolRequest> requests = poolRequestService.getAllRequests();

        List<Map<String, Object>> responseList = requests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("pickupLocation", req.getPickupLocation());
            map.put("dropLocation", req.getDestination() != null ? req.getDestination() : req.getDropLocation());
            map.put("totalSeats", req.getTotalSeats());

            int actualFilledSeats = req.getJoinedStudents() != null ? req.getJoinedStudents().size() : 0;
            map.put("filledSeats", actualFilledSeats);

            map.put("note", req.getNote());
            map.put("genderRestriction", req.getGenderRestriction());
            map.put("time", req.getTime());
            map.put("requestTime", req.getTime());
            map.put("createdAt", req.getCreatedAt());
            map.put("joinedStudents", req.getJoinedStudents());

            if (req.getStudent() != null) {
                map.put("studentId", req.getStudent().getStudentId());
                map.put("student", req.getStudent());
            }

            boolean isJoined = false;
            if (studentId != null && !studentId.trim().isEmpty()) {
                String cleanStudentId = studentId.trim().toLowerCase();
                isJoined = req.getJoinedStudents().stream()
                        .anyMatch(s -> s.getStudentId().equalsIgnoreCase(cleanStudentId));
            }

            map.put("isJoined", isJoined);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // 💡 Auto Matching / Suggestion Endpoint (WITH DEBUG LOGS)
    @GetMapping("/suggest")
    public ResponseEntity<List<PoolRequest>> getSuggestedPools(
            @RequestParam String pickup,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @RequestParam String studentId) {

        LocalDateTime searchTime = (time != null) ? time : LocalDateTime.now();
        LocalDateTime startTime = searchTime.minusMinutes(7);
        LocalDateTime endTime = searchTime.plusMinutes(7);

        System.out.println("================ DEBUG SUGGEST ================");
        System.out.println("Received Pickup: '" + pickup + "'");
        System.out.println("Received Destination: '" + destination + "'");
        System.out.println("Received Time: " + searchTime);
        System.out.println("Searching Window: " + startTime + " TO " + endTime);
        System.out.println("Searching StudentId: '" + studentId + "'");
        System.out.println("===============================================");

        List<PoolRequest> matches = poolRequestRepository.findMatchingPools(
                pickup, destination, startTime, endTime, studentId
        );

        return ResponseEntity.ok(matches);
    }

    // 👤 Get All Groups by Student ID
    @GetMapping("/user/{studentId}")
    public ResponseEntity<List<PoolRequest>> getRequestsByUser(@PathVariable String studentId) {
        return ResponseEntity.ok(poolRequestRepository.findAllGroupsByStudentId(studentId));
    }

    // ✏️ Update Ride Request Details
    @PutMapping("/{requestId}")
    public ResponseEntity<?> updateRequest(@PathVariable Long requestId, @RequestBody Map<String, Object> updates) {
        return poolRequestRepository.findById(requestId).map(req -> {
            if (updates.containsKey("pickupLocation")) {
                req.setPickupLocation((String) updates.get("pickupLocation"));
            }
            if (updates.containsKey("destination")) {
                req.setDestination((String) updates.get("destination"));
            }
            if (updates.containsKey("note")) {
                req.setNote((String) updates.get("note"));
            }
            Object timeObj = updates.get("requestTime") != null ? updates.get("requestTime") : updates.get("time");
            if (timeObj != null) {
                try {
                    String timeStr = timeObj.toString().trim();
                    if (timeStr.contains(".")) timeStr = timeStr.substring(0, timeStr.indexOf("."));
                    req.setTime(LocalDateTime.parse(timeStr));
                } catch (Exception ignored) {}
            }
            PoolRequest updated = poolRequestRepository.save(req);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🗑️ Delete Ride Request
    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long requestId) {
        if (poolRequestRepository.existsById(requestId)) {
            poolRequestRepository.deleteById(requestId);
            return ResponseEntity.ok("Deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // ➕ Join Group
    @PutMapping("/{id}/join")
    public ResponseEntity<?> joinRequest(@PathVariable Long id, @RequestParam String studentId) {
        try {
            Optional<PoolRequest> optionalRequest = poolRequestRepository.findById(id);
            if (!optionalRequest.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ride request not found!");
            }

            PoolRequest request = optionalRequest.get();
            Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);

            if (!studentOpt.isPresent() && studentId.contains("@")) {
                studentOpt = studentRepository.findByStudentId(studentId.split("@")[0].toUpperCase());
            }

            if (!studentOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found!");
            }

            Student student = studentOpt.get();

            boolean alreadyJoined = request.getJoinedStudents().stream()
                    .anyMatch(s -> s.getStudentId().equalsIgnoreCase(student.getStudentId()));

            if (alreadyJoined) {
                return ResponseEntity.badRequest().body("You are already in this group!");
            }

            if (request.getJoinedStudents().size() < request.getTotalSeats()) {
                request.addMember(student);
                request.setFilledSeats(request.getJoinedStudents().size());
                PoolRequest updatedRequest = poolRequestRepository.save(request);
                return ResponseEntity.ok(updatedRequest);
            } else {
                return ResponseEntity.badRequest().body("This ride request is already full!");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error joining request: " + e.getMessage());
        }
    }

    // ➖ Leave Group
    @PutMapping("/{id}/leave")
    public ResponseEntity<?> leaveRequest(@PathVariable Long id, @RequestParam String studentId) {
        try {
            Optional<PoolRequest> optionalRequest = poolRequestRepository.findById(id);
            if (!optionalRequest.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ride request not found!");
            }

            PoolRequest request = optionalRequest.get();
            Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);

            if (!studentOpt.isPresent() && studentId.contains("@")) {
                studentOpt = studentRepository.findByStudentId(studentId.split("@")[0].toUpperCase());
            }

            if (!studentOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found!");
            }

            Student student = studentOpt.get();

            boolean isCreator = request.getStudent() != null &&
                    request.getStudent().getStudentId().equalsIgnoreCase(student.getStudentId());

            boolean isMember = request.getJoinedStudents().stream()
                    .anyMatch(s -> s.getStudentId().equalsIgnoreCase(student.getStudentId()));

            if (!isMember && !isCreator) {
                return ResponseEntity.badRequest().body("You are not a member of this group!");
            }

            if (isMember) {
                request.removeMember(student);
            }

            if (isCreator) {
                if (!request.getJoinedStudents().isEmpty()) {
                    Student newCreator = request.getJoinedStudents().iterator().next();
                    request.setStudent(newCreator);
                    request.getJoinedStudents().remove(newCreator);
                } else {
                    poolRequestRepository.delete(request);
                    return ResponseEntity.ok("You left the group. Group was deleted as no members remained.");
                }
            }

            request.setFilledSeats(request.getAllMembers().size());
            PoolRequest updatedRequest = poolRequestRepository.save(request);

            return ResponseEntity.ok(updatedRequest);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error leaving request: " + e.getMessage());
        }
    }
}