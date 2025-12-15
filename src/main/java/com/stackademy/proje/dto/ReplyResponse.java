package com.stackademy.proje.dto;

import java.time.LocalDateTime;

public class ReplyResponse {
    private String id;
    private String postId;
    private String userId;
    private String userName;
    private String content;
    private String imageUrl;
    private boolean isDeleted;
    private LocalDateTime createdAt;

    // --- CONSTRUCTOR ---
    public ReplyResponse(String id, String postId, String userId, String userName,
            String content, String imageUrl, boolean isDeleted, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    // --- GETTER VE SETTERLAR ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
