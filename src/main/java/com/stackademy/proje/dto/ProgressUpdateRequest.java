package com.stackademy.proje.dto;

import java.util.UUID;

public class ProgressUpdateRequest {
    
    private UUID userId;
    private UUID contentId;
    private Integer currentSecond; // Kaçıncı saniyede?
    private Integer totalDuration; // Videonun toplam süresi (Yüzde hesaplamak için)

    // --- MANUEL GETTER VE SETTERLAR ---

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getContentId() { return contentId; }
    public void setContentId(UUID contentId) { this.contentId = contentId; }

    public Integer getCurrentSecond() { return currentSecond; }
    public void setCurrentSecond(Integer currentSecond) { this.currentSecond = currentSecond; }

    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }
}