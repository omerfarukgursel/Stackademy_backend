package com.stackademy.proje.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails { // <-- DEĞİŞİKLİK BURADA

    private String fullName;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    private String passwordHash;
    private String role; // STUDENT, TEACHER

    private String branch;
    private String schoolLevel;
    private String subscriptionPlan;

    private boolean isActive;
    private String activationCode;
    private String resetCode;

    @Column(columnDefinition = "integer default 0")
    private Integer activationAttempts = 0;

    @Column(columnDefinition = "integer default 0")
    private Integer resetCodeAttempts = 0;

    private java.time.LocalDateTime timeoutUntil; // Kullanıcı susturma bitiş zamanı

    // Sıralama ve ilerleme için
    @Column(columnDefinition = "integer default 0")
    private Integer totalScore = 0; // Toplam puan (quiz'lerden)
    private String lastTopic; // Son çalışılan konu
    private String lastCategory; // Son çalışılan kategori
    private java.time.LocalDateTime lastStudyTime; // Son çalışma zamanı

    // --- USER DETAILS METODLARI (SPRING SECURITY İÇİN) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Rol null ise boş liste dönmeyelim, hata alabiliriz. Varsayılan bir rol
        // atayalım.
        String authority = role != null ? role.toUpperCase() : "STUDENT";
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return passwordHash; // Şifremiz bu alanda
    }

    @Override
    public String getUsername() {
        return email; // Giriş yaparken EMAIL kullanıyoruz, o yüzden burası email dönmeli
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive; // Eğer hesap aktive edilmediyse Spring Security girişi otomatik engeller!
    }

    // --- MANUEL GETTER VE SETTERLAR (ESKİLERİ AYNEN KORUDUK) ---

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    } // getUsername yukarıda Override edildi

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSchoolLevel() {
        return schoolLevel;
    }

    public void setSchoolLevel(String schoolLevel) {
        this.schoolLevel = schoolLevel;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public java.time.LocalDateTime getTimeoutUntil() {
        return timeoutUntil;
    }

    public void setTimeoutUntil(java.time.LocalDateTime timeoutUntil) {
        this.timeoutUntil = timeoutUntil;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getLastTopic() {
        return lastTopic;
    }

    public void setLastTopic(String lastTopic) {
        this.lastTopic = lastTopic;
    }

    public String getLastCategory() {
        return lastCategory;
    }

    public void setLastCategory(String lastCategory) {
        this.lastCategory = lastCategory;
    }

    public java.time.LocalDateTime getLastStudyTime() {
        return lastStudyTime;
    }

    public void setLastStudyTime(java.time.LocalDateTime lastStudyTime) {
        this.lastStudyTime = lastStudyTime;
    }

    public Integer getActivationAttempts() {
        return activationAttempts != null ? activationAttempts : 0;
    }

    public void setActivationAttempts(Integer activationAttempts) {
        this.activationAttempts = activationAttempts;
    }

    public Integer getResetCodeAttempts() {
        return resetCodeAttempts != null ? resetCodeAttempts : 0;
    }

    public void setResetCodeAttempts(Integer resetCodeAttempts) {
        this.resetCodeAttempts = resetCodeAttempts;
    }
}