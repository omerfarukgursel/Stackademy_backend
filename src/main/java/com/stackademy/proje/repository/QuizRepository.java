package com.stackademy.proje.repository;

import com.stackademy.proje.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    // Topic'e göre denemeleri getir
    List<Quiz> findByTopic(String topic);

    // Kategori ve topic'e göre getir
    List<Quiz> findByCategoryAndTopic(String category, String topic);

    // Kategoriye göre getir
    List<Quiz> findByCategory(String category);

    // Öğretmene göre getir
    List<Quiz> findByCreatedBy(UUID createdBy);
}
