package backend.repository;

import backend.entity.PoolRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PoolRequestRepository extends JpaRepository<PoolRequest, Long> {

    List<PoolRequest> findByStudent_StudentId(String studentId);

    void deleteByTimeBefore(LocalDateTime time);

    // Auto Matching Suggestion Query
    @Query("SELECT p FROM PoolRequest p WHERE LOWER(p.pickupLocation) LIKE LOWER(CONCAT('%', :pickup, '%')) " +
            "AND LOWER(p.destination) LIKE LOWER(CONCAT('%', :destination, '%')) " +
            "AND p.time BETWEEN :startTime AND :endTime " +
            "AND p.filledSeats < p.totalSeats " +
            "AND p.student.studentId <> :currentStudentId")
    List<PoolRequest> findMatchingPools(
            @Param("pickup") String pickup,
            @Param("destination") String destination,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("currentStudentId") String currentStudentId
    );
}