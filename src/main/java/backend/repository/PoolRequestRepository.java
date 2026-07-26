package backend.repository;

import backend.entity.PoolRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PoolRequestRepository extends JpaRepository<PoolRequest, Long> {


    List<PoolRequest> findByStudentStudentId(String studentId);


    void deleteByCreatedAtBefore(LocalDateTime time);
}