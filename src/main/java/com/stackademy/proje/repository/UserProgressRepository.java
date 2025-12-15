package com.stackademy.proje.repository;

import com.stackademy.proje.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    
    // Öğrencinin bu derste daha önce bir kaydı var mı?
    Optional<UserProgress> findByUserIdAndContentId(UUID userId, UUID contentId);

    // Öğrencinin tüm izleme geçmişini getir
    List<UserProgress> findByUserId(UUID userId);
}