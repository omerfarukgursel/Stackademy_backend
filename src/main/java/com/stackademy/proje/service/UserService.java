package com.stackademy.proje.service;

import com.stackademy.proje.dto.StudentResponse;
import com.stackademy.proje.dto.TeacherResponse;
import com.stackademy.proje.dto.UserResponse;
import com.stackademy.proje.dto.UserUpdateRequest;
import com.stackademy.proje.entity.User;
import com.stackademy.proje.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private int convertLevelToInt(String level) {
        if (level == null)
            return 999;
        if (level.toLowerCase().contains("mezun"))
            return 13;
        if (level.toLowerCase().contains("hazırlık"))
            return 0;
        try {
            String numberOnly = level.replaceAll("[^0-9]", "");
            return numberOnly.isEmpty() ? 99 : Integer.parseInt(numberOnly);
        } catch (Exception e) {
            return 99;
        }
    }

    public List<StudentResponse> getSortedStudents() {
        List<User> users = userRepository.findByRole("STUDENT");
        List<StudentResponse> responseList = new ArrayList<>();
        for (User user : users) {
            responseList.add(new StudentResponse(
                    user.getId().toString(),
                    user.getFullName(),
                    user.getSubscriptionPlan(),
                    user.getSchoolLevel()));
        }
        responseList.sort(Comparator.comparingInt(s -> convertLevelToInt(s.getSchoolLevel())));
        return responseList;
    }

    public String getFormattedStudentList() {
        List<User> users = userRepository.findByRole("STUDENT");
        users.sort(Comparator.comparingInt(s -> convertLevelToInt(s.getSchoolLevel())));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-25s %-15s %-15s\n", "AD SOYAD", "PLAN", "SINIF"));
        sb.append("-------------------------------------------------------------\n");
        for (User user : users) {
            String uName = user.getFullName() != null ? user.getFullName() : "-";
            String plan = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : "-";
            String level = user.getSchoolLevel() != null ? user.getSchoolLevel() : "-";
            sb.append(String.format("%-25s %-15s %-15s\n", uName, plan, level));
        }
        return sb.toString();
    }

    public List<TeacherResponse> getAllTeachers() {
        List<User> users = userRepository.findByRole("TEACHER");
        List<TeacherResponse> responseList = new ArrayList<>();
        for (User user : users) {
            responseList.add(new TeacherResponse(
                    user.getId().toString(),
                    user.getFullName(),
                    user.getBranch()));
        }
        return responseList;
    }

    // --- PROFİL GETİRME ---
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
        return convertToUserResponse(user);
    }

    // --- PROFİL GÜNCELLEME (E-POSTA DEĞİŞİKLİĞİ KALDIRILDI) ---
    public UserResponse updateUserProfile(String currentEmail, UserUpdateRequest request) {

        // 1. Kullanıcıyı bul
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // 2. Sadece izin verilen alanları güncelle (Email yok)
        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getSchoolLevel() != null)
            user.setSchoolLevel(request.getSchoolLevel());

        // 3. Kaydet
        User updatedUser = userRepository.save(user);

        // 4. Dönüş
        return convertToUserResponse(updatedUser);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getSubscriptionPlan(),
                user.getSchoolLevel(),
                user.getBranch());
    }

    // --- KULLANICI SUSTURMA (ÖĞRETMEN İÇİN) ---
    public void timeoutUser(java.util.UUID userId, int minutes, String teacherEmail) {
        // Öğretmen kontrolü
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı: " + teacherEmail));

        if (!"TEACHER".equalsIgnoreCase(teacher.getRole())) {
            throw new RuntimeException("Bu işlemi sadece öğretmenler yapabilir!");
        }

        // Susturulacak kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        // Susturma zamanını ayarla
        user.setTimeoutUntil(java.time.LocalDateTime.now().plusMinutes(minutes));
        userRepository.save(user);
    }

    // --- PAKET YÜKSELTME ---
    public void upgradePackage(String userEmail, String packageType) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userEmail));

        user.setSubscriptionPlan(packageType);
        userRepository.save(user);
    }

    // --- TIMEOUT DURUMU KONTROLÜ ---
    public java.util.Map<String, Object> getTimeoutStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userEmail));

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        if (user.getTimeoutUntil() != null && user.getTimeoutUntil().isAfter(java.time.LocalDateTime.now())) {
            // Kullanıcı susturulmuş
            long remainingMinutes = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    user.getTimeoutUntil()).toMinutes();

            result.put("isTimedOut", true);
            result.put("remainingMinutes", remainingMinutes);
            result.put("timeoutUntil", user.getTimeoutUntil().toString());
        } else {
            result.put("isTimedOut", false);
            result.put("remainingMinutes", 0);
        }

        return result;
    }
}