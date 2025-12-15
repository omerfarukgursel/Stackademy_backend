package com.stackademy.proje.dto;

public class ActivationRequest {
    private String email;
    private String code;

    // --- GETTER VE SETTERLAR ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}