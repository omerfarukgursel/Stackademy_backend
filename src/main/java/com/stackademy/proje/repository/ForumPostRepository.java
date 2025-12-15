package com.stackademy.proje.repository;

import com.stackademy.proje.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {

    // Tüm postları yeniden eskiye sırala
    List<ForumPost> findAllByOrderByCreatedAtDesc();

    // Kategoriye göre filtrele ve sırala
    List<ForumPost> findByCategoryOrderByCreatedAtDesc(String category);
}