package com.stackademy.proje.entity;

import com.stackademy.proje.enums.ProgressStatus;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_progress")
public class UserProgress extends BaseEntity {

    private UUID userId;        // Hangi Öğrenci?
    private UUID contentId;     // Hangi Ders?

    @Enumerated(EnumType.STRING)
    private ProgressStatus status; // Devam ediyor mu, bitti mi?

    private Integer progressPercentage; // %0 ile %100 arası
    private Integer lastWatchedSecond;  // Videoda kalınan saniye (Örn: 120. saniye)
    
    // --- MANUEL GETTER VE SETTERLAR ---

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getContentId() { return contentId; }
    public void setContentId(UUID contentId) { this.contentId = contentId; }

    public ProgressStatus getStatus() { return status; }
    public void setStatus(ProgressStatus status) { this.status = status; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public Integer getLastWatchedSecond() { return lastWatchedSecond; }
    public void setLastWatchedSecond(Integer lastWatchedSecond) { this.lastWatchedSecond = lastWatchedSecond; }
}