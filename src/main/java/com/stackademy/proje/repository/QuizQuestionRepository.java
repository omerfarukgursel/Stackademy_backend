package com.stackademy.proje.repository;

import com.stackademy.proje.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {

    // Quiz ID'ye göre soruları getir (sıralı)
    List<QuizQuestion> findByQuizIdOrderByQuestionNumberAsc(UUID quizId);

    // Quiz'e ait soru sayısı
    int countByQuizId(UUID quizId);
}
