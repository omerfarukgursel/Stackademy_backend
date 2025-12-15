package com.stackademy.proje.dto;

public class LoginResponse {
    private String token;
    private String fullName;
    private String role;
    private String packageType;
    private String email;
    private String id;

    public LoginResponse() {
    }

    public LoginResponse(String token, String fullName, String role, String packageType, String email, String id) {
        this.token = token;
        this.fullName = fullName;
        this.role = role;
        this.packageType = packageType;
        this.email = email;
        this.id = id;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
