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

    /**
     * 1. Add a new friend connection directly (Bidirectional - Save both directions)
     */
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addFriend(@RequestBody Map<String, String> payload) {
        try {
            String userStudentId = payload.get("userStudentId");
            String friendStudentId = payload.get("friendStudentId");

            if (userStudentId == null || friendStudentId == null ||
                    userStudentId.trim().isEmpty() || friendStudentId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Both student IDs are required");
            }

            userStudentId = userStudentId.trim();
            friendStudentId = friendStudentId.trim();

            if (userStudentId.equalsIgnoreCase(friendStudentId)) {
                return ResponseEntity.badRequest().body("You cannot add yourself as a friend");
            }

            // Check if friend relationship already exists in either direction
            boolean alreadyFriends = friendRepository.existsByUserStudentIdAndFriendStudentId(userStudentId, friendStudentId)
                    || friendRepository.existsByUserStudentIdAndFriendStudentId(friendStudentId, userStudentId);

            if (alreadyFriends) {
                return ResponseEntity.badRequest().body("Already added as a friend");
            }

            // 1️⃣ Save relation User -> Friend
            Friend friend1 = new Friend();
            friend1.setUserStudentId(userStudentId);
            friend1.setFriendStudentId(friendStudentId);
            friend1.setStatus("ACCEPTED");
            friendRepository.save(friend1);

            // 2️⃣ Save reverse relation Friend -> User (so both users see each other after login)
            Friend friend2 = new Friend();
            friend2.setUserStudentId(friendStudentId);
            friend2.setFriendStudentId(userStudentId);
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
        if (studentId == null || studentId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String cleanStudentId = studentId.trim();
        List<Friend> friends = friendRepository.findByUserStudentIdAndStatus(cleanStudentId, "ACCEPTED");
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Friend f : friends) {
            Optional<Student> studentOpt = studentRepository.findByStudentId(f.getFriendStudentId());

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
        if (studentId == null || studentId.trim().isEmpty()) {
            return ResponseEntity.ok(0L);
        }
        long count = friendRepository.countByUserStudentIdAndStatus(studentId.trim(), "ACCEPTED");
        return ResponseEntity.ok(count);
    }

    /**
     * 4. Remove / Unfriend Endpoint (Deletes relationship in both directions)
     */
    @DeleteMapping("/remove")
    @Transactional
    public ResponseEntity<?> removeFriend(@RequestParam String userStudentId, @RequestParam String friendStudentId) {
        try {
            String cleanUser = userStudentId.trim();
            String cleanFriend = friendStudentId.trim();

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