package backend.controller;

import backend.entity.ChatMessage;
import backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 1. Send Message Endpoint
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> payload) {
        try {
            Long poolId = Long.parseLong(payload.get("poolId").toString());
            String studentId = payload.get("studentId").toString();
            String messageText = payload.get("message").toString();

            ChatMessage savedMessage = chatService.saveMessage(poolId, studentId, messageText);
            return ResponseEntity.ok(savedMessage);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Console eke exact error stacktrace eka balaganna
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
    }

    // 2. Fetch Messages for a Pool
    @GetMapping("/{poolId}")
    public ResponseEntity<?> getMessagesByPoolId(@PathVariable Long poolId) {
        try {
            List<ChatMessage> messages = chatService.getMessagesByPoolId(poolId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching messages: " + e.getMessage());
        }
    }
}