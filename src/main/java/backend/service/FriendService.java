package backend.service;

import backend.entity.Friend;
import backend.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    // 1. Direct Add Friend (Instant ACCEPTED status)
    public boolean addFriendDirect(String userStudentId, String friendStudentId) {
        // Check if the friendship already exists
        Optional<Friend> existing = friendRepository.findByUserStudentIdAndFriendStudentId(userStudentId, friendStudentId);
        if (existing.isPresent()) {
            return false;
        }

        Friend friend = new Friend();
        friend.setUserStudentId(userStudentId);      // Corrected: Matches entity field name
        friend.setFriendStudentId(friendStudentId);  // Corrected: Matches entity field name
        friend.setStatus("ACCEPTED");               // Set status directly to ACCEPTED

        friendRepository.save(friend);
        return true;
    }

    // 2. Remove / Unfriend Student
    @Transactional
    public boolean removeFriend(String userStudentId, String friendStudentId) {
        // Check if a relationship exists in either direction (A -> B or B -> A)
        Optional<Friend> relationship1 = friendRepository.findByUserStudentIdAndFriendStudentId(userStudentId, friendStudentId);
        Optional<Friend> relationship2 = friendRepository.findByUserStudentIdAndFriendStudentId(friendStudentId, userStudentId);

        if (relationship1.isPresent()) {
            friendRepository.delete(relationship1.get());
            return true;
        } else if (relationship2.isPresent()) {
            friendRepository.delete(relationship2.get());
            return true;
        }
        return false;
    }
}