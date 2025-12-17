package com.stackademy.proje.controller;

import com.stackademy.proje.dto.StudentResponse;
import com.stackademy.proje.dto.TeacherResponse;
import com.stackademy.proje.dto.UserUpdateRequest;
import com.stackademy.proje.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Öğrenci Listesi (Sadece Öğretmen Görür)
    @PreAuthorize("hasAuthority('TEACHER')")
    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getStudents() {
        return ResponseEntity.ok(userService.getSortedStudents());
    }

    // 2. Düz Metin Çıktısı (Sadece Öğretmen Görür)
    @PreAuthorize("hasAuthority('TEACHER')")
    @GetMapping("/print_students")
    public ResponseEntity<String> printStudents() {
        return ResponseEntity.ok(userService.getFormattedStudentList());
    }

    // 3. Öğretmen Listesi (Herkes Görür)
    @GetMapping("/teachers")
    public ResponseEntity<List<TeacherResponse>> getTeachers() {
        return ResponseEntity.ok(userService.getAllTeachers());
    }

    // 4. Profil Güncelleme (Herkes Kendi Profilini Günceller)
    @PutMapping("/update")
    public ResponseEntity<StudentResponse> updateProfile(@RequestBody UserUpdateRequest request, Principal principal) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getName(), request));
    }

    // 5. Kullanıcı Sustur (Sadece Öğretmen)
    // POST /api/users/{id}/timeout
    @PreAuthorize("hasAuthority('TEACHER')")
    @PostMapping("/{id}/timeout")
    public ResponseEntity<Map<String, String>> timeoutUser(
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> request,
            Principal principal) {

        int minutes = request.getOrDefault("minutes", 30);
        userService.timeoutUser(id, minutes, principal.getName());

        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Kullanıcı " + minutes + " dakika susturuldu"));
    }

    // 6. Paket Yükselt
    // POST /api/users/upgrade
    @PostMapping("/upgrade")
    public ResponseEntity<Map<String, Object>> upgradePackage(
            @RequestBody Map<String, String> request,
            Principal principal) {

        String packageType = request.get("packageType");

        // Geçerli paket kontrolü
        if (packageType == null ||
                (!packageType.equals("FREE") && !packageType.equals("PREMIUM") && !packageType.equals("EXCLUSIVE"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Geçersiz paket tipi. FREE, PREMIUM veya EXCLUSIVE olmalı."));
        }

        userService.upgradePackage(principal.getName(), packageType);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Paket başarıyla " + packageType + " olarak güncellendi"));
    }

    // 7. Kullanıcının timeout durumunu kontrol et
    // GET /api/users/me/timeout-status
    @GetMapping("/me/timeout-status")
    public ResponseEntity<Map<String, Object>> getMyTimeoutStatus(Principal principal) {
        return ResponseEntity.ok(userService.getTimeoutStatus(principal.getName()));
    }
}