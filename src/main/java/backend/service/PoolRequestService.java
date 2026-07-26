package backend.service;

import backend.entity.PoolRequest;
import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PoolRequestService {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    public PoolRequest saveRequest(PoolRequest request) {
        if (request.getTime() == null) {
            request.setTime(LocalDateTime.now());
        }
        return poolRequestRepository.save(request);
    }

    public List<PoolRequest> getAllRequests() {
        return poolRequestRepository.findAll();
    }

    // Auto Matching Suggestion Logic
    public List<PoolRequest> getSuggestedPools(String pickup, String destination, LocalDateTime requestTime, String currentStudentId) {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }

        // Find matching groups within a 30-minute time window before and after
        LocalDateTime startTime = requestTime.minusMinutes(30);
        LocalDateTime endTime = requestTime.plusMinutes(30);

        return poolRequestRepository.findMatchingPools(
                pickup,
                destination,
                startTime,
                endTime,
                currentStudentId
        );
    }
}