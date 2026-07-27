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

    // Clean & Normalize Student ID (Email එකක් ආවොත් Index No එක පමණක් වෙන් කර ගනී)
    private String cleanStudentId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) return "";
        String trimmed = rawId.trim();
        if (trimmed.contains("@")) {
            trimmed = trimmed.split("@")[0];
        }
        return trimmed.toUpperCase();
    }

    // 1. Direct Add Friend (Instant ACCEPTED status - Bidirectional Save)
    @Transactional
    public boolean addFriendDirect(String userStudentId, String friendStudentId) {
        String cleanUser = cleanStudentId(userStudentId);
        String cleanFriend = cleanStudentId(friendStudentId);

        if (cleanUser.isEmpty() || cleanFriend.isEmpty() || cleanUser.equalsIgnoreCase(cleanFriend)) {
            return false;
        }

        // Check if friendship already exists in either direction
        boolean exists = friendRepository.existsByUserStudentIdAndFriendStudentId(cleanUser, cleanFriend)
                || friendRepository.existsByUserStudentIdAndFriendStudentId(cleanFriend, cleanUser);

        if (exists) {
            return false;
        }

        // 1️⃣ Save User -> Friend
        Friend friend1 = new Friend();
        friend1.setUserStudentId(cleanUser);
        friend1.setFriendStudentId(cleanFriend);
        friend1.setStatus("ACCEPTED");
        friendRepository.save(friend1);

        // 2️⃣ Save Friend -> User (Bidirectional)
        Friend friend2 = new Friend();
        friend2.setUserStudentId(cleanFriend);
        friend2.setFriendStudentId(cleanUser);
        friend2.setStatus("ACCEPTED");
        friendRepository.save(friend2);

        return true;
    }

    // 2. Remove / Unfriend Student (Deletes relationship in BOTH directions)
    @Transactional
    public boolean removeFriend(String userStudentId, String friendStudentId) {
        String cleanUser = cleanStudentId(userStudentId);
        String cleanFriend = cleanStudentId(friendStudentId);

        if (cleanUser.isEmpty() || cleanFriend.isEmpty()) {
            return false;
        }

        // Check and fetch relationships in both directions
        Optional<Friend> relationship1 = friendRepository.findByUserStudentIdAndFriendStudentId(cleanUser, cleanFriend);
        Optional<Friend> relationship2 = friendRepository.findByUserStudentIdAndFriendStudentId(cleanFriend, cleanUser);

        boolean removed = false;

        if (relationship1.isPresent()) {
            friendRepository.delete(relationship1.get());
            removed = true;
        }
        if (relationship2.isPresent()) {
            friendRepository.delete(relationship2.get());
            removed = true;
        }

        return removed;
    }
}