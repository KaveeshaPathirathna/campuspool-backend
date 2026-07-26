package backend.repository;

import backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 Request ID එකට අදාළ Messages වෙලාව අනුව පිළිවෙළට ලබා ගැනීමට
    List<ChatMessage> findByRequestIdOrderByTimestampAsc(Long requestId);
}