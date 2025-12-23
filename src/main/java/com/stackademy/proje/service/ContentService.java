package com.stackademy.proje.service;

import com.stackademy.proje.entity.Content;
import com.stackademy.proje.repository.ContentRepository;
// --- DÜZELTME BURADA ---
// entity.ContentType yerine enums.ContentType kullanıyoruz
import com.stackademy.proje.enums.ContentType;
import com.stackademy.proje.enums.AccessLevel;
// -----------------------
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final CloudStorageService cloudStorageService;

    // Constructor Injection
    public ContentService(ContentRepository contentRepository, CloudStorageService cloudStorageService) {
        this.contentRepository = contentRepository;
        this.cloudStorageService = cloudStorageService;
    }

    public Content addContent(String title, String category, String topic, String type,
            String accessLevel, String description, String uploaderEmail,
            MultipartFile file, String preUploadedUrl) throws IOException {

        // 1. Dosya URL'sini belirle: preUploadedUrl varsa onu kullan, yoksa dosyayı
        // yükle
        String publicUrl;
        if (preUploadedUrl != null && !preUploadedUrl.isEmpty()) {
            publicUrl = preUploadedUrl;
        } else if (file != null && !file.isEmpty()) {
            publicUrl = cloudStorageService.uploadFile(file);
        } else {
            throw new IllegalArgumentException("Dosya veya preUploadedUrl gerekli!");
        }

        // 2. Veritabanı nesnesini oluştur
        Content content = new Content();
        content.setTitle(title);
        content.setCategory(category);
        content.setTopic(topic);

        // String -> ENUM Çevrimi (trim + toUpperCase ile güvenli hale getirildi)
        content.setType(ContentType.valueOf(type.trim().toUpperCase()));
        content.setAccessLevel(AccessLevel.valueOf(accessLevel.trim().toUpperCase()));

        // uploaderEmail olarak sakla (UUID parse yok!)
        content.setUploaderId(uploaderEmail);

        content.setDescription(description);
        content.setFileUrl(publicUrl);

        return contentRepository.save(content);
    }

    // İçerikleri topic ve type'a göre getir
    public List<Content> getContentsByTopicAndType(String topic, String type) {
        ContentType contentType = ContentType.valueOf(type.toUpperCase());
        return contentRepository.findByTopicAndType(topic, contentType);
    }

    // İçerikleri sadece topic'e göre getir
    public List<Content> getContentsByTopic(String topic) {
        return contentRepository.findByTopic(topic);
    }

    // Tüm içerikleri getir
    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    // İçerik sil (sadece öğretmen - kendi yüklediği veya herhangi biri)
    public void deleteContent(UUID contentId, String teacherEmail) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("İçerik bulunamadı: " + contentId));

        // Cloud Storage'dan dosyayı sil
        if (content.getFileUrl() != null && !content.getFileUrl().isEmpty()) {
            cloudStorageService.deleteFile(content.getFileUrl());
        }

        // Veritabanından sil
        contentRepository.deleteById(contentId);
    }
}