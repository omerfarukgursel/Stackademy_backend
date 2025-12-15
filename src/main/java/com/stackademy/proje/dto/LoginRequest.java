package com.stackademy.proje.dto;

public class LoginRequest {
    private String email;
    private String password;

    // --- GETTER VE SETTERLAR ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}