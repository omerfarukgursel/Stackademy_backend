package com.stackademy.proje.dto;

import java.util.UUID;

public class ContentRequest {
    
    private String title;
    private String category;
    private String topic;
    private String type;
    private String accessLevel; 
    private String fileUrl;
    private String description;
    private UUID uploaderId; 

    // --- MANUEL GETTER VE SETTERLAR ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // Yeni Getter/Setter
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getUploaderId() { return uploaderId; }
    public void setUploaderId(UUID uploaderId) { this.uploaderId = uploaderId; }
}