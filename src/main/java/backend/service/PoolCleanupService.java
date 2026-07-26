package backend.service;

import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PoolCleanupService {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    // Scheduled task running every 10 minutes to auto-delete requests older than 12 hours
    @Scheduled(fixedRate = 600000) // 600,000 ms = 10 minutes
    @Transactional
    public void deleteExpiredPoolRequests() {
        LocalDateTime cutOffTime = LocalDateTime.now().minusHours(12);
        poolRequestRepository.deleteByCreatedAtBefore(cutOffTime);
        System.out.println("Auto-deleted pool requests created before: " + cutOffTime);
    }
}