package backend.repository;

import backend.entity.PoolRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PoolRequestRepository extends JpaRepository<PoolRequest, Long> {

    // 1. Find all ride requests created by OR joined by a specific student
    @Query("SELECT DISTINCT p FROM PoolRequest p LEFT JOIN p.joinedStudents s " +
            "WHERE LOWER(p.student.studentId) = LOWER(:studentId) " +
            "OR LOWER(s.studentId) = LOWER(:studentId)")
    List<PoolRequest> findAllGroupsByStudentId(@Param("studentId") String studentId);

    // 2. Custom Query for Pool Suggestions / Auto Matching
    @Query("SELECT DISTINCT p FROM PoolRequest p " +
            "WHERE LOWER(TRIM(p.pickupLocation)) LIKE LOWER(CONCAT('%', TRIM(:pickup), '%')) " +
            "AND LOWER(TRIM(p.destination)) LIKE LOWER(CONCAT('%', TRIM(:destination), '%')) " +
            "AND p.time BETWEEN :startTime AND :endTime " +
            "AND (p.student IS NULL OR LOWER(p.student.studentId) != LOWER(:studentId)) " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM p.joinedStudents js WHERE LOWER(js.studentId) = LOWER(:studentId)" +
            ")")
    List<PoolRequest> findMatchingPools(
            @Param("pickup") String pickup,
            @Param("destination") String destination,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("studentId") String studentId
    );

    // 3. Find expired rides by event time
    List<PoolRequest> findByTimeBefore(LocalDateTime time);

    // 4. Clear Join Table entries first
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM pool_request_joined_students WHERE pool_request_id IN (" +
            "SELECT id FROM pool_requests WHERE request_time < :time)", nativeQuery = true)
    void deleteJoinedStudentsByTimeBefore(@Param("time") LocalDateTime time);

    // 5. Delete expired pool requests directly
    @Modifying
    @Transactional
    @Query("DELETE FROM PoolRequest p WHERE p.time < :time")
    void deleteByTimeBefore(@Param("time") LocalDateTime time);
}