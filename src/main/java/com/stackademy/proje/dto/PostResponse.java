package com.stackademy.proje.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class PostResponse {
    private String id;
    private String userId;
    private String userName;
    private String title;
    private String content;
    private String imageUrl;
    private String category;

    @JsonProperty("isSolved")
    private boolean isSolved;

    private int commentCount;
    private String accessLevel;
    private LocalDateTime createdAt;

    // --- CONSTRUCTOR ---
    public PostResponse(String id, String userId, String userName, String title,
            String content, String imageUrl, String category,
            boolean isSolved, int commentCount, String accessLevel, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isSolved = isSolved;
        this.commentCount = commentCount;
        this.accessLevel = accessLevel;
        this.createdAt = createdAt;
    }

    // --- GETTER VE SETTERLAR ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
