package com.stackademy.proje.controller;

import com.stackademy.proje.entity.Content;
import com.stackademy.proje.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    // İçerikleri topic ve/veya type'a göre listele
    @GetMapping
    public ResponseEntity<List<Content>> getContents(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String type) {

        // topic ve type ikisi de varsa
        if (topic != null && type != null) {
            List<Content> contents = contentService.getContentsByTopicAndType(topic, type);
            return ResponseEntity.ok(contents);
        }

        // Sadece topic varsa - topic'e göre filtrele
        if (topic != null) {
            List<Content> contents = contentService.getContentsByTopic(topic);
            return ResponseEntity.ok(contents);
        }

        // Parametre yoksa tüm içerikleri döndür
        List<Content> allContents = contentService.getAllContents();
        return ResponseEntity.ok(allContents);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addContent(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("topic") String topic,
            @RequestParam("type") String type,
            @RequestParam("accessLevel") String accessLevel,
            @RequestParam("description") String description,
            @RequestParam(value = "uploaderId", required = false) String uploaderId, // Opsiyonel - geri uyumluluk
            @RequestParam("file") MultipartFile file,
            Principal principal) { // JWT'den kullanıcı bilgisi

        try {
            // Uploader email'ini JWT'den al (öncelikli), yoksa request param'dan al
            String uploaderEmail;
            if (principal != null && principal.getName() != null) {
                uploaderEmail = principal.getName(); // JWT'deki email
                System.out.println("Upload isteği alındı (JWT): " + title + " - Uploader: " + uploaderEmail);
            } else if (uploaderId != null && !uploaderId.isEmpty()) {
                uploaderEmail = uploaderId; // Geri uyumluluk için
                System.out.println("Upload isteği alındı (Param): " + title + " - Uploader: " + uploaderEmail);
            } else {
                return ResponseEntity.status(400).body("Hata: Kullanıcı kimliği alınamadı. Lütfen tekrar giriş yapın.");
            }

            Content savedContent = contentService.addContent(
                    title, category, topic, type, accessLevel, description, uploaderEmail, file);
            return ResponseEntity.ok(savedContent);

        } catch (IllegalArgumentException e) {
            // Enum parse hataları için 400 Bad Request
            e.printStackTrace();
            return ResponseEntity.status(400).body("Geçersiz parametre: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (errorMessage == null)
                errorMessage = e.toString();

            String clientMessage = "Sunucu Hatası: " + errorMessage;
            if (errorMessage.contains("gcp-key.json")) {
                clientMessage = "Google Cloud Key dosyası okunamadı: " + errorMessage;
            } else if (errorMessage.contains("Connection refused")) {
                clientMessage = "Veritabanına bağlanılamadı. IP izni kontrol edilmeli.";
            }

            return ResponseEntity.status(500).body(clientMessage);
        }
    }

    // İçerik sil (sadece öğretmen)
    // DELETE /api/contents/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable UUID id, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("Yetkisiz erişim!");
            }

            contentService.deleteContent(id, principal.getName());
            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "İçerik başarıyla silindi"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Sunucu hatası: " + e.getMessage());
        }
    }
}