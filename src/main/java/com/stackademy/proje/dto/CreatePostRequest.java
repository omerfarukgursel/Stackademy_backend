package com.stackademy.proje.dto;

import java.util.UUID;

public class CreatePostRequest {
    private String title;
    private String content;
    private UUID userId; // Soruyu soran kim?
    private String category; // Kategori (TYT, AYT, Matematik, vb.)

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}