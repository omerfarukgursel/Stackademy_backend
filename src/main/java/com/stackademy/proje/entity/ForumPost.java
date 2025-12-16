package com.stackademy.proje.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "forum_posts")
public class ForumPost extends BaseEntity {

    private String title; // Sorunun Başlığı
    private String content; // Sorunun Detayı
    private UUID userId; // Soruyu soran kişinin ID'si
    private String imageUrl; // Soru görseli URL'i (opsiyonel)
    private String category; // Kategori (TYT, AYT, Matematik, Fizik, Kimya, Biyoloji, Diğer)
    private boolean isSolved; // Çözüldü mü?
    private String accessLevel; // Erişim seviyesi (FREE, PREMIUM, EXCLUSIVE) - sadece öğretmenler için

    // --- MANUEL GETTER VE SETTERLAR ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}