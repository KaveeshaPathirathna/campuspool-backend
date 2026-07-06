package backend.repository;

import backend.entity.PoolRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolRequestRepository extends JpaRepository<PoolRequest, Long> {
}