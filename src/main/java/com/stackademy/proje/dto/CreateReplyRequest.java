package com.stackademy.proje.dto;

import java.util.UUID;

public class CreateReplyRequest {
    private String content;
    private UUID userId; // Cevabı yazan kim?
    private UUID postId; // Hangi soruya cevap yazılıyor?
    private String imageUrl; // Görsel URL'si (opsiyonel)

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
}