package com.stackademy.proje.repository;

import com.stackademy.proje.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    // Öğrenci bu quiz'i daha önce çözmüş mü?
    boolean existsByQuizIdAndStudentIdAndIsCompletedTrue(UUID quizId, UUID studentId);

    // Öğrencinin bu quiz için denemesini getir
    Optional<QuizAttempt> findByQuizIdAndStudentId(UUID quizId, UUID studentId);

    // Quiz silinirse ona ait denemeleri de sil
    void deleteByQuizId(UUID quizId);
}
