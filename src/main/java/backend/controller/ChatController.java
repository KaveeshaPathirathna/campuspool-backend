package backend.controller;

import backend.entity.ChatMessage;
import backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // Fetch all messages for a specific ride request
    @GetMapping("/{requestId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long requestId) {
        return ResponseEntity.ok(chatMessageRepository.findByRequestIdOrderByTimestampAsc(requestId));
    }

    // Send and save a new message
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(message);
        return ResponseEntity.ok(saved);
    }
}