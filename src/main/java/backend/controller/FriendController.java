package backend.controller;

import backend.entity.Friend;
import backend.entity.Student;
import backend.repository.FriendRepository;
import backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*") // Allows cross-origin requests from Flutter Web
public class FriendController {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private StudentRepository studentRepository;

    // Helper Method: Clean & Normalize Student ID (Email එකක් ආවොත් Index No එක පමණක් වෙන් කර ගනී)
    private String cleanStudentId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) return "";
        String trimmed = rawId.trim();
        if (trimmed.contains("@")) {
            trimmed = trimmed.split("@")[0];
        }
        return trimmed.toUpperCase();
    }

    /**
     * 1. Add a new friend connection directly (Bidirectional - Save both directions)
     * Handles both JSON Body and URL Query Parameters gracefully to prevent HTTP 400
     */
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addFriend(
            @RequestBody(required = false) Map<String, String> payload,
            @RequestParam(required = false) String userStudentIdParam,
            @RequestParam(required = false) String friendStudentIdParam) {
        try {
            String userStudentId = null;
            String friendStudentId = null;

            // Extract from JSON Payload if available
            if (payload != null) {
                userStudentId = payload.get("userStudentId");
                friendStudentId = payload.get("friendStudentId");
            }

            // Fallback to URL Query Params if JSON payload is missing
            if (userStudentId == null) userStudentId = userStudentIdParam;
            if (friendStudentId == null) friendStudentId = friendStudentIdParam;

            // Clean IDs (e.g. as20240001@sci.sjp.ac.lk -> AS20240001)
            String cleanUser = cleanStudentId(userStudentId);
            String cleanFriend = cleanStudentId(friendStudentId);

            if (cleanUser.isEmpty() || cleanFriend.isEmpty()) {
                return ResponseEntity.badRequest().body("Both student IDs are required");
            }

            if (cleanUser.equalsIgnoreCase(cleanFriend)) {
                return ResponseEntity.badRequest().body("You cannot add yourself as a friend");
            }

            // Verify both students actually exist in the database
            Optional<Student> userOpt = studentRepository.findByStudentId(cleanUser);
            Optional<Student> friendOpt = studentRepository.findByStudentId(cleanFriend);

            if (userOpt.isEmpty() || friendOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("One or both students do not exist in the system");
            }

            // Check if friend relationship already exists in either direction
            boolean alreadyFriends = friendRepository.existsByUserStudentIdAndFriendStudentId(cleanUser, cleanFriend)
                    || friendRepository.existsByUserStudentIdAndFriendStudentId(cleanFriend, cleanUser);

            if (alreadyFriends) {
                return ResponseEntity.badRequest().body("Already added as a friend");
            }

            // 1️⃣ Save relation User -> Friend
            Friend friend1 = new Friend();
            friend1.setUserStudentId(cleanUser);
            friend1.setFriendStudentId(cleanFriend);
            friend1.setStatus("ACCEPTED");
            friendRepository.save(friend1);

            // 2️⃣ Save reverse relation Friend -> User
            Friend friend2 = new Friend();
            friend2.setUserStudentId(cleanFriend);
            friend2.setFriendStudentId(cleanUser);
            friend2.setStatus("ACCEPTED");
            friendRepository.save(friend2);

            return new ResponseEntity<>("Friend added successfully", HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding friend: " + e.getMessage());
        }
    }

    /**
     * 2. Get all accepted friends with profile details for a given student ID
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getFriendsWithDetails(@PathVariable String studentId) {
        String cleanUser = cleanStudentId(studentId);
        if (cleanUser.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Friend> friends = friendRepository.findByUserStudentIdAndStatus(cleanUser, "ACCEPTED");
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Friend f : friends) {
            String friendId = cleanStudentId(f.getFriendStudentId());
            Optional<Student> studentOpt = studentRepository.findByStudentId(friendId);

            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", s.getStudentId());
                map.put("name", s.getName());
                map.put("campusMail", s.getCampusMail());
                map.put("faculty", s.getFaculty());
                map.put("gender", s.getGender());
                responseList.add(map);
            }
        }

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    /**
     * 3. Get Friend Count for Profile UI
     */
    @GetMapping("/count/{studentId}")
    public ResponseEntity<Long> getFriendCount(@PathVariable String studentId) {
        String cleanUser = cleanStudentId(studentId);
        if (cleanUser.isEmpty()) {
            return ResponseEntity.ok(0L);
        }
        long count = friendRepository.countByUserStudentIdAndStatus(cleanUser, "ACCEPTED");
        return ResponseEntity.ok(count);
    }

    /**
     * 4. Remove / Unfriend Endpoint (Deletes relationship in both directions)
     */
    @DeleteMapping("/remove")
    @Transactional
    public ResponseEntity<?> removeFriend(@RequestParam String userStudentId, @RequestParam String friendStudentId) {
        try {
            String cleanUser = cleanStudentId(userStudentId);
            String cleanFriend = cleanStudentId(friendStudentId);

            if (cleanUser.isEmpty() || cleanFriend.isEmpty()) {
                return ResponseEntity.badRequest().body("Both student IDs are required");
            }

            Optional<Friend> rel1 = friendRepository.findByUserStudentIdAndFriendStudentId(cleanUser, cleanFriend);
            Optional<Friend> rel2 = friendRepository.findByUserStudentIdAndFriendStudentId(cleanFriend, cleanUser);

            boolean removed = false;
            if (rel1.isPresent()) {
                friendRepository.delete(rel1.get());
                removed = true;
            }
            if (rel2.isPresent()) {
                friendRepository.delete(rel2.get());
                removed = true;
            }

            if (removed) {
                return ResponseEntity.ok("Friend removed successfully");
            }

            return ResponseEntity.badRequest().body("Friend relationship not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing friend: " + e.getMessage());
        }
    }
}