package backend.repository;

import backend.entity.PoolRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PoolRequestRepository extends JpaRepository<PoolRequest, Long> {

    // Custom query to find pending requests within a specific 15-minute time window at the same location
    List<PoolRequest> findByPickupLocationAndStatusAndExpectedArrivalTimeBetween(
            String pickupLocation,
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}