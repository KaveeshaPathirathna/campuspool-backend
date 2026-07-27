package backend.service;

import backend.entity.Friend;
import backend.entity.PoolRequest;
import backend.repository.FriendRepository;
import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PoolRequestService {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    @Autowired
    private FriendRepository friendRepository;

    public PoolRequest saveRequest(PoolRequest request) {
        if (request.getTime() == null) {
            request.setTime(LocalDateTime.now());
        }
        return poolRequestRepository.save(request);
    }

    public List<PoolRequest> getAllRequests() {
        return poolRequestRepository.findAll();
    }

    // Helper method: Email වලින් Index ID එක පමනක් වෙන් කර ගැනීම
    private String cleanStudentId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) return "";
        if (rawId.contains("@")) {
            return rawId.split("@")[0].toUpperCase();
        }
        return rawId.trim().toUpperCase();
    }

    // Smart Ride Matching Suggestion Logic: ±7 Minutes Window + Friend Priority
    public List<PoolRequest> getSuggestedPools(String pickup, String destination, LocalDateTime requestTime, String currentStudentId) {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }

        String cleanId = cleanStudentId(currentStudentId);

        // 1. Calculate ±7 minutes time window
        LocalDateTime startTime = requestTime.minusMinutes(7);
        LocalDateTime endTime = requestTime.plusMinutes(7);

        // 2. Fetch matching pools from DB based on pickup, destination, and ±7 min time window
        List<PoolRequest> matchedPools = poolRequestRepository.findMatchingPools(
                pickup != null ? pickup.trim() : "",
                destination != null ? destination.trim() : "",
                startTime,
                endTime,
                cleanId
        );

        // 3. Filter out FULL pools (ආසන පිරුණු pools ඉවත් කිරීම)
        matchedPools.removeIf(p -> p.getJoinedStudents() != null && p.getJoinedStudents().size() >= p.getTotalSeats());

        // 4. Get all accepted friends for the current student
        Set<String> friendIds = new HashSet<>();

        List<Friend> friendsList = friendRepository.findByUserStudentIdAndStatus(cleanId, "ACCEPTED");
        if (friendsList != null) {
            for (Friend f : friendsList) {
                friendIds.add(cleanStudentId(f.getFriendStudentId()));
            }
        }

        // 5. Friend Priority Sorting: Friends' created groups appear FIRST in the list
        matchedPools.sort((p1, p2) -> {
            boolean p1IsFriend = p1.getStudent() != null && friendIds.contains(cleanStudentId(p1.getStudent().getStudentId()));
            boolean p2IsFriend = p2.getStudent() != null && friendIds.contains(cleanStudentId(p2.getStudent().getStudentId()));

            if (p1IsFriend && !p2IsFriend) return -1; // p1 (friend) comes first
            if (!p1IsFriend && p2IsFriend) return 1;  // p2 (friend) comes first
            return 0; // Keep normal time order if both/neither are friends
        });

        return matchedPools;
    }
}