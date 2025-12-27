package com.stackademy.proje.service;

import com.stackademy.proje.dto.*;
import com.stackademy.proje.entity.User;
import com.stackademy.proje.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    private static final int MAX_CODE_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final EmailValidationService emailValidationService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService,
            EmailValidationService emailValidationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.emailValidationService = emailValidationService;
    }

    // --- 1. KAYIT OL ---
    public String register(RegisterRequest request) {
        // Email domain kontrolü (DNS MX)
        if (!emailValidationService.isValidEmailDomain(request.getEmail())) {
            throw new RuntimeException("Geçersiz e-posta domaini! Lütfen geçerli bir e-posta adresi kullanın.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kayıtlı!");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole("STUDENT");
        user.setSubscriptionPlan("FREE");
        user.setSchoolLevel(request.getSchoolLevel());

        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setActivationCode(code);
        user.setActivationAttempts(0);
        user.setActive(false);

        userRepository.save(user);

        try {
            emailService.sendActivationEmail(user.getEmail(), code);
        } catch (Exception e) {
            System.out.println("Mail hatası: " + e.getMessage());
        }

        return "Kayıt başarılı! Lütfen mailinize gelen 6 haneli kodu doğrulayın.";
    }

    // --- 2. AKTİVASYON ---
    public String activateUser(ActivationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (user.isActive()) {
            return "Bu hesap zaten aktif.";
        }

        // Deneme limiti kontrolü
        if (user.getActivationAttempts() >= MAX_CODE_ATTEMPTS) {
            throw new RuntimeException("Çok fazla hatalı deneme! Lütfen yeni kod talep edin.");
        }

        if (!request.getCode().equals(user.getActivationCode())) {
            user.setActivationAttempts(user.getActivationAttempts() + 1);
            userRepository.save(user);
            int remaining = MAX_CODE_ATTEMPTS - user.getActivationAttempts();
            throw new RuntimeException("Hatalı aktivasyon kodu! Kalan deneme hakkı: " + remaining);
        }

        // Başarılı aktivasyon - sayacı sıfırla
        user.setActivationAttempts(0);
        user.setActive(true);
        user.setActivationCode(null);
        userRepository.save(user);

        return "Hesap başarıyla aktive edildi! Artık giriş yapabilirsiniz.";
    }

    // --- 2.1 AKTİVASYON KODU TEKRAR GÖNDER ---
    public String resendActivationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (user.isActive()) {
            return "Bu hesap zaten aktif.";
        }

        // Yeni kod oluştur ve deneme sayacını sıfırla
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setActivationCode(code);
        user.setActivationAttempts(0);
        userRepository.save(user);

        try {
            emailService.sendActivationEmail(email, code);
        } catch (Exception e) {
            System.out.println("Mail hatası: " + e.getMessage());
        }

        return "Yeni aktivasyon kodu gönderildi.";
    }

    // --- 3. GİRİŞ YAP (SADECE EMAIL İLE) ---
    public LoginResponse login(LoginRequest request, String targetRole) {

        // Sadece email ile kullanıcı bul
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Şifre Kontrolü
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Şifre hatalı!");
        }

        // Aktivasyon Kontrolü
        if (!user.isActive()) {
            throw new RuntimeException("Hesabınız aktif değil! Lütfen mail onayı yapın.");
        }

        // Rol Kontrolü
        if (!user.getRole().equals(targetRole)) {
            throw new RuntimeException(
                    "Hatalı giriş! Siz bir " + user.getRole() + " hesabısınız, lütfen doğru sekmeden giriş yapın.");
        }

        // Token Üretimi
        String token = jwtService.generateToken(user);

        // LoginResponse döndür - kullanıcı bilgileriyle birlikte
        return new LoginResponse(
                token,
                user.getFullName(),
                user.getRole(),
                user.getSubscriptionPlan(),
                user.getEmail(),
                user.getId().toString());
    }

    // --- 4. ŞİFREMİ UNUTTUM (KOD GÖNDER) ---
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu email adresiyle kayıtlı kullanıcı bulunamadı."));

        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setResetCode(code);
        user.setResetCodeAttempts(0); // Yeni kod gönderildiğinde sayacı sıfırla
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), code);
        } catch (Exception e) {
            System.out.println("Mail hatası: " + e.getMessage());
        }

        return "Sıfırlama kodu mailinize gönderildi.";
    }

    // --- 5. ŞİFREMİ UNUTTUM (KOD DOĞRULA) ---
    public String verifyResetCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // Deneme limiti kontrolü
        if (user.getResetCodeAttempts() >= MAX_CODE_ATTEMPTS) {
            throw new RuntimeException("Çok fazla hatalı deneme! Lütfen yeni kod talep edin.");
        }

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            user.setResetCodeAttempts(user.getResetCodeAttempts() + 1);
            userRepository.save(user);
            int remaining = MAX_CODE_ATTEMPTS - user.getResetCodeAttempts();
            throw new RuntimeException("Hatalı veya geçersiz kod! Kalan deneme hakkı: " + remaining);
        }

        // Başarılı - sayacı sıfırla
        user.setResetCodeAttempts(0);
        userRepository.save(user);

        return "Kod doğrulandı.";
    }

    // --- 5.1 ŞİFRE SIFIRLAMA KODU TEKRAR GÖNDER ---
    public String resendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setResetCode(code);
        user.setResetCodeAttempts(0);
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(email, code);
        } catch (Exception e) {
            System.out.println("Mail hatası: " + e.getMessage());
        }

        return "Yeni sıfırlama kodu gönderildi.";
    }

    // --- 6. ŞİFREMİ UNUTTUM (YENİ ŞİFRE) ---
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Şifreler uyuşmuyor!");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeAttempts(0);
        userRepository.save(user);

        return "Şifreniz başarıyla değiştirildi. Giriş yapabilirsiniz.";
    }
}