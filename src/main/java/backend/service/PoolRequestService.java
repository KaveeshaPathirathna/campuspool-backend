package backend.service;

import backend.entity.PoolRequest;
import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PoolRequestService {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    public PoolRequest createRequest(PoolRequest request) {
        request.setStatus("PENDING");
        request.setGroupId(null); // Initially, no group is assigned

        // 1. Save the incoming ride request to the database first
        PoolRequest savedRequest = poolRequestRepository.save(request);

        // 2. Define a 15-minute time window (15 mins before and 15 mins after expected time)
        LocalDateTime startTime = savedRequest.getExpectedArrivalTime().minusMinutes(15);
        LocalDateTime endTime = savedRequest.getExpectedArrivalTime().plusMinutes(15);

        // 3. Search for other PENDING requests matching the same pickup location and time window
        List<PoolRequest> waitingRequests = poolRequestRepository
                .findByPickupLocationAndStatusAndExpectedArrivalTimeBetween(
                        savedRequest.getPickupLocation(),
                        "PENDING",
                        startTime,
                        endTime
                );

        // 4. If 2 or more matching requests are found (including the current one), group them up
        if (waitingRequests.size() >= 2) {
            // Generate a completely unique identifier for the chat group
            String uniqueGroupId = UUID.randomUUID().toString();

            // Update status to MATCHED and assign the same groupId to all matched requests
            for (PoolRequest req : waitingRequests) {
                req.setStatus("MATCHED");
                req.setGroupId(uniqueGroupId);
                poolRequestRepository.save(req); // Save updated changes to the database
            }
        }

        return savedRequest;
    }

    public List<PoolRequest> getAllRequests() {
        return poolRequestRepository.findAll();
    }
}