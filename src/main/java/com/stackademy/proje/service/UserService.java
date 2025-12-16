package com.stackademy.proje.service;

import com.stackademy.proje.dto.StudentResponse;
import com.stackademy.proje.dto.TeacherResponse;
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

    // --- PROFİL GÜNCELLEME (E-POSTA DEĞİŞİKLİĞİ KALDIRILDI) ---
    public StudentResponse updateUserProfile(String currentEmail, UserUpdateRequest request) {

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
        return new StudentResponse(
                updatedUser.getId().toString(),
                updatedUser.getFullName(),
                updatedUser.getSubscriptionPlan(),
                updatedUser.getSchoolLevel());
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
}