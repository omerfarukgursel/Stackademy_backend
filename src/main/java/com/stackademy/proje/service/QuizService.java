package com.stackademy.proje.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackademy.proje.dto.*;
import com.stackademy.proje.entity.*;
import com.stackademy.proje.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            QuizAttemptRepository attemptRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Yeni deneme oluştur (öğretmen)
     */
    @Transactional
    public QuizResponse createQuiz(CreateQuizRequest request, UUID teacherId) {
        // Maks 20 soru kontrolü
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new RuntimeException("En az 1 soru eklenmelidir!");
        }
        if (request.getQuestions().size() > 20) {
            throw new RuntimeException("Maksimum 20 soru eklenebilir!");
        }

        // Quiz oluştur
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setCategory(request.getCategory());
        quiz.setTopic(request.getTopic());
        quiz.setCreatedBy(teacherId);
        Quiz savedQuiz = quizRepository.save(quiz);

        // Soruları kaydet
        for (CreateQuizRequest.QuestionData qData : request.getQuestions()) {
            QuizQuestion question = new QuizQuestion();
            question.setQuizId(savedQuiz.getId());
            question.setQuestionNumber(qData.getQuestionNumber());
            question.setImageUrl(qData.getImageUrl());
            question.setCorrectAnswer(qData.getCorrectAnswer().toUpperCase());
            questionRepository.save(question);
        }

        return convertToResponse(savedQuiz, false);
    }

    /**
     * Topic'e göre denemeleri listele
     */
    public List<QuizResponse> getQuizzesByTopic(String topic) {
        List<Quiz> quizzes = quizRepository.findByTopic(topic);
        return quizzes.stream()
                .map(q -> convertToResponse(q, false))
                .collect(Collectors.toList());
    }

    /**
     * Deneme detayı getir (sorularla birlikte)
     * showAnswers: true ise cevapları göster (testi bitirdikten sonra)
     */
    public QuizResponse getQuizById(UUID quizId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Deneme bulunamadı: " + quizId));

        // Öğrenci testi çözmüş mü kontrol et
        boolean hasCompleted = attemptRepository.existsByQuizIdAndStudentIdAndIsCompletedTrue(quizId, studentId);

        return convertToResponse(quiz, hasCompleted);
    }

    /**
     * Deneme cevaplarını gönder ve puanla
     */
    @Transactional
    public QuizResultResponse submitQuiz(UUID quizId, UUID studentId, SubmitQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Deneme bulunamadı: " + quizId));

        // Daha önce çözmüş mü kontrol et
        if (attemptRepository.existsByQuizIdAndStudentIdAndIsCompletedTrue(quizId, studentId)) {
            throw new RuntimeException("Bu denemeyi zaten çözdünüz!");
        }

        // Soruları getir
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByQuestionNumberAsc(quizId);

        // Cevapları karşılaştır
        int correctCount = 0;
        int wrongCount = 0;

        for (QuizQuestion question : questions) {
            String studentAnswer = request.getAnswers().get(question.getQuestionNumber());
            if (studentAnswer != null && studentAnswer.equalsIgnoreCase(question.getCorrectAnswer())) {
                correctCount++;
            } else if (studentAnswer != null && !studentAnswer.isEmpty()) {
                wrongCount++;
            }
            // Boş cevaplar sayılmaz
        }

        int score = correctCount * 5;

        // Quiz attempt kaydet
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setStudentId(studentId);
        attempt.setCorrectCount(correctCount);
        attempt.setWrongCount(wrongCount);
        attempt.setScore(score);
        attempt.setCompleted(true);
        attempt.setCompletedAt(LocalDateTime.now());

        // Cevapları JSON olarak kaydet
        try {
            attempt.setAnswers(objectMapper.writeValueAsString(request.getAnswers()));
        } catch (JsonProcessingException e) {
            attempt.setAnswers("{}");
        }

        attemptRepository.save(attempt);

        // Öğrencinin toplam puanını güncelle
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı: " + studentId));

        int newTotalScore = student.getTotalScore() + score;
        student.setTotalScore(newTotalScore);
        userRepository.save(student);

        // Email gönder
        sendResultEmail(student, quiz, correctCount, wrongCount, score, newTotalScore);

        // Response oluştur
        QuizResultResponse result = new QuizResultResponse();
        result.setQuizId(quizId.toString());
        result.setQuizTitle(quiz.getTitle());
        result.setTotalQuestions(questions.size());
        result.setCorrectCount(correctCount);
        result.setWrongCount(wrongCount);
        result.setScore(score);
        result.setTotalScore(newTotalScore);

        return result;
    }

    /**
     * Sıralama listesi
     */
    public List<RankingResponse> getRankings() {
        List<User> students = userRepository.findByRoleOrderByTotalScoreDesc("STUDENT");

        List<RankingResponse> rankings = new ArrayList<>();
        int rank = 1;
        for (User student : students) {
            RankingResponse r = new RankingResponse();
            r.setRank(rank++);
            r.setStudentId(student.getId().toString());
            r.setFullName(student.getFullName());
            r.setTotalScore(student.getTotalScore());
            rankings.add(r);
        }
        return rankings;
    }

    /**
     * Öğrencinin kendi sıralaması
     */
    public RankingResponse getMyRanking(UUID studentId) {
        List<User> students = userRepository.findByRoleOrderByTotalScoreDesc("STUDENT");

        int rank = 1;
        for (User student : students) {
            if (student.getId().equals(studentId)) {
                RankingResponse r = new RankingResponse();
                r.setRank(rank);
                r.setStudentId(student.getId().toString());
                r.setFullName(student.getFullName());
                r.setTotalScore(student.getTotalScore());
                return r;
            }
            rank++;
        }
        throw new RuntimeException("Öğrenci bulunamadı: " + studentId);
    }

    /**
     * Son çalışılan konuyu kaydet
     */
    public void updateLastTopic(UUID userId, String category, String topic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        user.setLastCategory(category);
        user.setLastTopic(topic);
        user.setLastStudyTime(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Son çalışılan konuyu getir
     */
    public Map<String, Object> getLastTopic(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        Map<String, Object> result = new HashMap<>();
        result.put("lastCategory", user.getLastCategory());
        result.put("lastTopic", user.getLastTopic());
        result.put("lastStudyTime", user.getLastStudyTime());
        return result;
    }

    /**
     * Öğrencinin bu quiz için önceki denemesini getir
     */
    public QuizResultResponse getMyAttempt(UUID quizId, UUID studentId) {
        Optional<QuizAttempt> attemptOpt = attemptRepository.findByQuizIdAndStudentId(quizId, studentId);

        if (attemptOpt.isEmpty() || !attemptOpt.get().isCompleted()) {
            return null;
        }

        QuizAttempt attempt = attemptOpt.get();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Deneme bulunamadı: " + quizId));

        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByQuestionNumberAsc(quizId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı: " + studentId));

        QuizResultResponse result = new QuizResultResponse();
        result.setQuizId(quizId.toString());
        result.setQuizTitle(quiz.getTitle());
        result.setTotalQuestions(questions.size());
        result.setCorrectCount(attempt.getCorrectCount());
        result.setWrongCount(attempt.getWrongCount());
        result.setScore(attempt.getScore());
        result.setTotalScore(student.getTotalScore());

        return result;
    }

    /**
     * Denemeyi ve bağlı verileri sil (Öğretmen)
     */
    @Transactional
    public void deleteQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Deneme bulunamadı: " + quizId));

        // 1. Soruları sil
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByQuestionNumberAsc(quizId);
        questionRepository.deleteAll(questions);

        // 2. Denemeleri (öğrenci sonuçlarını) sil
        attemptRepository.deleteByQuizId(quizId);

        // 3. Quizi sil
        quizRepository.delete(quiz);
    }

    // --- YARDIMCI METODLAR ---

    private QuizResponse convertToResponse(Quiz quiz, boolean showAnswers) {
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId().toString());
        response.setTitle(quiz.getTitle());
        response.setCategory(quiz.getCategory());
        response.setTopic(quiz.getTopic());
        response.setCreatedAt(quiz.getCreatedAt());

        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByQuestionNumberAsc(quiz.getId());
        response.setQuestionCount(questions.size());

        List<QuizResponse.QuestionResponse> questionResponses = new ArrayList<>();
        for (QuizQuestion q : questions) {
            QuizResponse.QuestionResponse qr = new QuizResponse.QuestionResponse();
            qr.setId(q.getId().toString());
            qr.setQuestionNumber(q.getQuestionNumber());
            qr.setImageUrl(q.getImageUrl());
            if (showAnswers) {
                qr.setCorrectAnswer(q.getCorrectAnswer());
            }
            questionResponses.add(qr);
        }
        response.setQuestions(questionResponses);

        return response;
    }

    private void sendResultEmail(User student, Quiz quiz, int correctCount, int wrongCount, int score, int totalScore) {
        String subject = "Deneme Sonucunuz: " + quiz.getTitle();
        String body = String.format(
                "Merhaba %s,\n\n" +
                        "Tamamladığınız deneme: %s\n\n" +
                        "Sonuçlarınız:\n" +
                        "- Doğru: %d\n" +
                        "- Yanlış: %d\n" +
                        "- Aldığınız Puan: %d\n" +
                        "- Toplam Puanınız: %d\n\n" +
                        "Başarılar dileriz!\n" +
                        "Stackademy Ekibi",
                student.getFullName(),
                quiz.getTitle(),
                correctCount,
                wrongCount,
                score,
                totalScore);

        try {
            emailService.sendEmail(student.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Email gönderilemedi: " + e.getMessage());
        }
    }
}
