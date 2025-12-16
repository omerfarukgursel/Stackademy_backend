package com.stackademy.proje.controller;

import com.stackademy.proje.dto.*;
import com.stackademy.proje.entity.User;
import com.stackademy.proje.repository.UserRepository;
import com.stackademy.proje.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;

    public QuizController(QuizService quizService, UserRepository userRepository) {
        this.quizService = quizService;
        this.userRepository = userRepository;
    }

    // ========== QUIZ ENDPOINT'LERİ ==========

    /**
     * Yeni deneme oluştur (öğretmen)
     * POST /api/quizzes
     */
    @PostMapping("/quizzes")
    public ResponseEntity<?> createQuiz(@RequestBody CreateQuizRequest request, Principal principal) {
        try {
            User teacher = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            if (!"TEACHER".equals(teacher.getRole())) {
                return ResponseEntity.status(403).body("Bu işlemi sadece öğretmenler yapabilir!");
            }

            QuizResponse response = quizService.createQuiz(request, teacher.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Topic'e göre denemeleri listele
     * GET /api/quizzes?topic=Limit
     */
    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizResponse>> getQuizzes(@RequestParam String topic) {
        List<QuizResponse> quizzes = quizService.getQuizzesByTopic(topic);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Deneme detayı getir (sorularla birlikte)
     * GET /api/quizzes/{id}
     */
    @GetMapping("/quizzes/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable UUID id, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            QuizResponse response = quizService.getQuizById(id, user.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deneme cevaplarını gönder
     * POST /api/quizzes/{id}/submit
     */
    @PostMapping("/quizzes/{id}/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable UUID id,
            @RequestBody SubmitQuizRequest request,
            Principal principal) {
        try {
            User student = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            QuizResultResponse result = quizService.submitQuiz(id, student.getId(), request);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== SIRALAMA ENDPOINT'LERİ ==========

    /**
     * Tüm öğrenci sıralaması
     * GET /api/rankings
     */
    @GetMapping("/rankings")
    public ResponseEntity<List<RankingResponse>> getRankings() {
        List<RankingResponse> rankings = quizService.getRankings();
        return ResponseEntity.ok(rankings);
    }

    /**
     * Kendi sıralamam
     * GET /api/rankings/my
     */
    @GetMapping("/rankings/my")
    public ResponseEntity<?> getMyRanking(Principal principal) {
        try {
            User student = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            RankingResponse ranking = quizService.getMyRanking(student.getId());
            return ResponseEntity.ok(ranking);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== KALDIĞIMIZ YER ENDPOINT'LERİ ==========

    /**
     * Son konuyu kaydet
     * POST /api/progress/last-topic
     */
    @PostMapping("/progress/last-topic")
    public ResponseEntity<?> updateLastTopic(
            @RequestBody Map<String, String> request,
            Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            String category = request.get("category");
            String topic = request.get("topic");

            quizService.updateLastTopic(user.getId(), category, topic);
            return ResponseEntity.ok(Map.of("message", "Son konu kaydedildi"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Son konuyu getir
     * GET /api/progress/last-topic
     */
    @GetMapping("/progress/last-topic")
    public ResponseEntity<?> getLastTopic(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            Map<String, Object> lastTopic = quizService.getLastTopic(user.getId());
            return ResponseEntity.ok(lastTopic);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
