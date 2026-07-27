package backend.repository;

import backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Option 1: Standard JPA Naming Convention (Recommended)
    // Spring Data JPA navigates through `poolRequest` entity to its `id` property
    List<ChatMessage> findByPoolRequestIdOrderByTimestampAsc(Long poolRequestId);


    // Option 2: Custom JPQL Query
    // Use this if you specifically want to keep the method name `findByRequestIdOrderByTimestampAsc`
    @Query("SELECT c FROM ChatMessage c WHERE c.poolRequest.id = :requestId ORDER BY c.timestamp ASC")
    List<ChatMessage> findByRequestIdOrderByTimestampAsc(@Param("requestId") Long requestId);
}