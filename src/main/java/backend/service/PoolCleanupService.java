package backend.service;

import backend.entity.PoolRequest;
import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PoolCleanupService {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    // Run scheduled cleanup (e.g., every 30 minutes or 1 hour)
    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void deleteExpiredPoolRequests() {
        LocalDateTime now = LocalDateTime.now();

        // Fetch all expired requests
        List<PoolRequest> expiredRequests = poolRequestRepository.findByTimeBefore(now);

        if (!expiredRequests.isEmpty()) {
            for (PoolRequest pool : expiredRequests) {
                //  FIX: Delete join table references first before deleting the parent pool
                pool.getJoinedStudents().clear();
                poolRequestRepository.save(pool); // Flush join table clearing
                poolRequestRepository.delete(pool); // Delete pool request safely
            }
        }
    }
}