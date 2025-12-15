package com.stackademy.proje.controller;

import com.stackademy.proje.dto.*;
import com.stackademy.proje.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activate(@RequestBody ActivationRequest request) {
        return ResponseEntity.ok(authService.activateUser(request));
    }

    @PostMapping("/login/student")
    public ResponseEntity<?> loginStudent(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request, "STUDENT"));
        } catch (Exception e) {
            e.printStackTrace();
            return handleAuthError(e);
        }
    }

    @PostMapping("/login/teacher")
    public ResponseEntity<?> loginTeacher(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request, "TEACHER"));
        } catch (Exception e) {
            e.printStackTrace();
            return handleAuthError(e);
        }
    }

    private ResponseEntity<String> handleAuthError(Exception e) {
        String msg = e.getMessage();
        if (msg == null)
            msg = e.toString();

        if (msg.contains("Connection refused") || msg.contains("The connection attempt failed")) {
            return ResponseEntity.status(500)
                    .body("Veritabanı bağlantı hatası! Cloud SQL IP izni veya SSL ayarını kontrol edin.");
        }
        return ResponseEntity.status(500).body("Giriş başarısız: " + msg);
    }

    // --- ŞİFRE SIFIRLAMA ENDPOINTLERİ ---

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<String> verifyResetCode(@RequestBody VerifyResetCodeRequest request) {
        return ResponseEntity.ok(authService.verifyResetCode(request.getEmail(), request.getCode()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}