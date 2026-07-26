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
     * 1. Add a new friend connection directly (Instant ACCEPTED)
     */
    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestBody Map<String, String> payload) {
        try {
            String userStudentId = payload.get("userStudentId");
            String friendStudentId = payload.get("friendStudentId");

            if (userStudentId == null || friendStudentId == null) {
                return ResponseEntity.badRequest().body("Both student IDs are required");
            }

            if (userStudentId.equals(friendStudentId)) {
                return ResponseEntity.badRequest().body("You cannot add yourself as a friend");
            }

            // Check if friend relationship already exists
            boolean alreadyFriends = friendRepository.existsByUserStudentIdAndFriendStudentId(userStudentId, friendStudentId);
            if (alreadyFriends) {
                return ResponseEntity.badRequest().body("Already added as a friend");
            }

            Friend friend = new Friend();
            friend.setUserStudentId(userStudentId);
            friend.setFriendStudentId(friendStudentId);
            friend.setStatus("ACCEPTED"); // Direct ACCEPTED

            friendRepository.save(friend);
            return new ResponseEntity<>("Friend added successfully", HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * 2. Get all accepted friends with profile details for a given student ID
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getFriendsWithDetails(@PathVariable String studentId) {
        List<Friend> friends = friendRepository.findByUserStudentIdAndStatus(studentId, "ACCEPTED");
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
        long count = friendRepository.countByUserStudentIdAndStatus(studentId, "ACCEPTED");
        return ResponseEntity.ok(count);
    }

    /**
     * 4. Remove / Unfriend Endpoint
     */
    @DeleteMapping("/remove")
    @Transactional
    public ResponseEntity<?> removeFriend(@RequestParam String userStudentId, @RequestParam String friendStudentId) {
        try {
            Optional<Friend> rel1 = friendRepository.findByUserStudentIdAndFriendStudentId(userStudentId, friendStudentId);
            Optional<Friend> rel2 = friendRepository.findByUserStudentIdAndFriendStudentId(friendStudentId, userStudentId);

            if (rel1.isPresent()) {
                friendRepository.delete(rel1.get());
                return ResponseEntity.ok("Friend removed successfully");
            } else if (rel2.isPresent()) {
                friendRepository.delete(rel2.get());
                return ResponseEntity.ok("Friend removed successfully");
            }

            return ResponseEntity.badRequest().body("Friend relationship not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}