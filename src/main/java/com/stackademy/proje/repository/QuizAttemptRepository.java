package com.stackademy.proje.repository;

import com.stackademy.proje.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    // Öğrencinin belirli bir quiz için denemesi
    Optional<QuizAttempt> findByQuizIdAndStudentId(UUID quizId, UUID studentId);

    // Öğrencinin tüm denemeleri
    List<QuizAttempt> findByStudentId(UUID studentId);

    // Quiz için tüm denemeler
    List<QuizAttempt> findByQuizId(UUID quizId);

    // Öğrencinin bu quiz'i çözüp çözmediğini kontrol
    boolean existsByQuizIdAndStudentIdAndIsCompletedTrue(UUID quizId, UUID studentId);

    // Quiz silindiğinde ilgili denemeleri sil
    void deleteByQuizId(UUID quizId);
}
