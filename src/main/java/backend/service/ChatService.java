package backend.service;

import backend.entity.ChatMessage;
import backend.entity.PoolRequest;
import backend.entity.Student;
import backend.repository.ChatMessageRepository;
import backend.repository.PoolRequestRepository;
import backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    /**
     * Saves a new chat message sent by a student within a specific ride pool.
     */
    public ChatMessage saveMessage(Long poolId, String studentId, String messageText) {
        // 1. Fetch student sender from DB
        Student sender = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with ID " + studentId + " not found!"));

        // 2. Fetch ride pool request from DB
        PoolRequest pool = poolRequestRepository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Ride Pool #" + poolId + " not found!"));

        // 3. Create and populate new ChatMessage object
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setPoolRequest(pool);
        chatMessage.setSender(sender);
        chatMessage.setMessage(messageText);
        chatMessage.setTimestamp(LocalDateTime.now()); // 👈 Fixed: Passed LocalDateTime directly without .toString()

        // 4. Save to database
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * Retrieves all messages for a specific pool, ordered by timestamp ascending.
     */
    public List<ChatMessage> getMessagesByPoolId(Long poolId) {
        return chatMessageRepository.findByPoolRequestIdOrderByTimestampAsc(poolId);
    }
}