package com.stackademy.proje.controller;

import com.stackademy.proje.dto.CreatePostRequest;
import com.stackademy.proje.dto.CreateReplyRequest;
import com.stackademy.proje.dto.PostResponse;
import com.stackademy.proje.dto.ReplyResponse;
import com.stackademy.proje.entity.User;
import com.stackademy.proje.repository.UserRepository;
import com.stackademy.proje.service.CloudStorageService;
import com.stackademy.proje.service.ForumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ForumController {

    private final ForumService forumService;
    private final CloudStorageService cloudStorageService;
    private final UserRepository userRepository;

    public ForumController(ForumService forumService, CloudStorageService cloudStorageService,
            UserRepository userRepository) {
        this.forumService = forumService;
        this.cloudStorageService = cloudStorageService;
        this.userRepository = userRepository;
    }

    // ========== POST ENDPOINT'LERİ ==========

    // 1. Tüm postları getir (kategori filtresi opsiyonel)
    // GET /api/posts?category=Matematik
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(forumService.getPosts(category));
    }

    // 2. Tek post detayı
    // GET /api/posts/{id}
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(forumService.getPostById(id));
    }

    // 3. Yeni post oluştur
    // POST /api/posts
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request, Principal principal) {
        // Token'dan kullanıcıyı al ve userId'yi set et
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                request.setUserId(user.getId());
            }
        }
        return ResponseEntity.ok(forumService.createPost(request));
    }

    // 4. Çözüldü işaretle (sadece soru sahibi)
    // PATCH /api/posts/{id}/solved
    @PatchMapping("/posts/{id}/solved")
    public ResponseEntity<PostResponse> markAsSolved(@PathVariable UUID id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return ResponseEntity.ok(forumService.markAsSolved(id, user.getId()));
    }

    // 5. Post sil (sadece öğretmen)
    // DELETE /api/posts/{id}
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable UUID id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        forumService.deletePost(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Post başarıyla silindi"));
    }

    // ========== REPLY ENDPOINT'LERİ ==========

    // 6. Bir postun cevaplarını getir
    // GET /api/posts/{postId}/replies
    @GetMapping("/posts/{postId}/replies")
    public ResponseEntity<List<ReplyResponse>> getReplies(@PathVariable UUID postId) {
        return ResponseEntity.ok(forumService.getRepliesByPostId(postId));
    }

    // 7. Cevap yaz
    // POST /api/replies
    @PostMapping("/replies")
    public ResponseEntity<ReplyResponse> createReply(@RequestBody CreateReplyRequest request, Principal principal) {
        // Token'dan kullanıcıyı al ve userId'yi set et
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                request.setUserId(user.getId());
            }
        }
        return ResponseEntity.ok(forumService.createReply(request));
    }

    // 8. Cevap sil (sadece öğretmen)
    // DELETE /api/replies/{id}
    @DeleteMapping("/replies/{id}")
    public ResponseEntity<Map<String, String>> deleteReply(@PathVariable UUID id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        forumService.deleteReply(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Cevap başarıyla silindi"));
    }

    // ========== UPLOAD ENDPOINT'İ ==========

    // 9. Görsel yükle (resim)
    // POST /api/upload
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudStorageService.uploadFile(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Dosya yüklenemedi: " + e.getMessage()));
        }
    }
}