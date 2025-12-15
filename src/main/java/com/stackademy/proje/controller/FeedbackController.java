package com.stackademy.proje.controller;

import com.stackademy.proje.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final EmailService emailService;

    public FeedbackController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendFeedback(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String content = request.get("content");

        if (to == null || subject == null || content == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tüm alanlar zorunludur"));
        }

        try {
            emailService.sendFeedbackEmail(to, subject, content);
            return ResponseEntity.ok(Map.of("message", "Geri bildirim başarıyla gönderildi"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Mail gönderilemedi: " + e.getMessage()));
        }
    }
}
