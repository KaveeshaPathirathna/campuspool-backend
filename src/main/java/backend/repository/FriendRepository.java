package backend.repository;

import backend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 1. Existing Methods
    List<Friend> findByUserStudentIdAndStatus(String userStudentId, String status);
    boolean existsByUserStudentIdAndFriendStudentId(String userStudentId, String friendStudentId);

    // 2. ✅ Added for Friend Count (Profile UI)
    long countByUserStudentIdAndStatus(String userStudentId, String status);

    // 3. ✅ Added for Unfriend / Remove Friend
    Optional<Friend> findByUserStudentIdAndFriendStudentId(String userStudentId, String friendStudentId);
}