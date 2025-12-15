package com.stackademy.proje.repository;

import com.stackademy.proje.entity.Content;
import com.stackademy.proje.enums.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {

    // Service katmanında kullandığımız metodlar burada olmak ZORUNDA

    List<Content> findByCategory(String category);

    List<Content> findByTopic(String topic);

    List<Content> findByUploaderId(String uploaderId);

    // Konu ve type'a göre içerik listele
    List<Content> findByTopicAndType(String topic, ContentType type);

    // Kategori, konu ve type'a göre içerik listele
    List<Content> findByCategoryAndTopicAndType(String category, String topic, ContentType type);

    // Tüm içerikleri getir
    List<Content> findAll();
}