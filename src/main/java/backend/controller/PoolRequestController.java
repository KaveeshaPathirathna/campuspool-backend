package backend.controller;

import backend.entity.PoolRequest;
import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class PoolRequestController {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    // Create new Ride Request
    @PostMapping("/create")
    public ResponseEntity<?> createRequest(@RequestBody PoolRequest request) {
        try {
            PoolRequest savedRequest = poolRequestRepository.save(request);
            return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving request: " + e.getMessage());
        }
    }

    // Get all Ride Requests
    @GetMapping
    public ResponseEntity<List<PoolRequest>> getAllRequests() {
        return ResponseEntity.ok(poolRequestRepository.findAll());
    }

    // Get requests created by a specific user
    @GetMapping("/user/{studentId}")
    public ResponseEntity<List<PoolRequest>> getRequestsByUser(@PathVariable String studentId) {
        return ResponseEntity.ok(poolRequestRepository.findByStudentStudentId(studentId));
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

    // 🔑 Join Ride Request (Increments filledSeats by 1)
    @PutMapping("/{id}/join")
    public ResponseEntity<?> joinRequest(@PathVariable Long id) {
        try {
            Optional<PoolRequest> optionalRequest = poolRequestRepository.findById(id);
            if (optionalRequest.isPresent()) {
                PoolRequest request = optionalRequest.get();

                // Check if seats are still available
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

    // 🚪 Leave Ride Request (Decrements filledSeats by 1)
    @PutMapping("/{id}/leave")
    public ResponseEntity<?> leaveRequest(@PathVariable Long id) {
        try {
            Optional<PoolRequest> optionalRequest = poolRequestRepository.findById(id);
            if (optionalRequest.isPresent()) {
                PoolRequest request = optionalRequest.get();

                // Ensure filledSeats doesn't go below 0
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