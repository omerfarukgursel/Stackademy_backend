package com.stackademy.proje.service;

import com.stackademy.proje.dto.ProgressUpdateRequest;
import com.stackademy.proje.entity.UserProgress;
import com.stackademy.proje.enums.ProgressStatus;
import com.stackademy.proje.repository.UserProgressRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final com.stackademy.proje.repository.UserRepository userRepository;

    public ProgressService(UserProgressRepository progressRepository,
            com.stackademy.proje.repository.UserRepository userRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    public String updateProgress(ProgressUpdateRequest request) {
        // 1. Önce bu kullanıcının bu derste kaydı var mı bakalım
        Optional<UserProgress> existingProgress = progressRepository.findByUserIdAndContentId(request.getUserId(),
                request.getContentId());

        UserProgress progress;

        if (existingProgress.isPresent()) {
            // Kayıt varsa onu alıp güncelleyeceğiz
            progress = existingProgress.get();
        } else {
            // Kayıt yoksa sıfırdan oluşturacağız
            progress = new UserProgress();
            progress.setUserId(request.getUserId());
            progress.setContentId(request.getContentId());
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }

        // 2. Yüzdeyi Hesapla (Şu anki saniye / Toplam süre * 100)
        int percentage = 0;
        if (request.getTotalDuration() > 0) {
            percentage = (request.getCurrentSecond() * 100) / request.getTotalDuration();
        }

        progress.setLastWatchedSecond(request.getCurrentSecond());
        progress.setProgressPercentage(percentage);

        // 3. Eğer %90'ı geçildiyse "TAMAMLANDI" sayalım
        if (percentage >= 90) {
            progress.setStatus(ProgressStatus.COMPLETED);
        } else {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }

        progressRepository.save(progress);

        return "İlerleme kaydedildi: %" + percentage;
    }

    // --- SON KALINAN KONU ---

    public void updateLastTopic(String email, String category, String topic) {
        com.stackademy.proje.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        user.setLastCategory(category);
        user.setLastTopic(topic);
        user.setLastStudyTime(java.time.LocalDateTime.now());

        userRepository.save(user);
    }

    public java.util.Map<String, String> getLastTopic(String email) {
        com.stackademy.proje.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("lastCategory", user.getLastCategory());
        response.put("lastTopic", user.getLastTopic());
        response.put("lastStudyTime", user.getLastStudyTime() != null ? user.getLastStudyTime().toString() : null);

        return response;
    }
}