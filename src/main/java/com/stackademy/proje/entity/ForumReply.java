package com.stackademy.proje.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "forum_replies")
public class ForumReply extends BaseEntity {

    private String content; // Cevap Metni
    private UUID userId; // Cevabı yazan kişi
    private UUID postId; // Hangi soruya cevap verildi?
    private String imageUrl; // Cevap görseli URL'i (opsiyonel)
    private boolean isDeleted; // Soft delete için

    // --- MANUEL GETTER VE SETTERLAR ---

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

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}