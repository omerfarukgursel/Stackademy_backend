package com.stackademy.proje.dto;

public class TeacherResponse {
    private String id;
    private String fullName;
    private String branch;

    // --- CONSTRUCTOR ---
    public TeacherResponse(String id, String fullName, String branch) {
        this.id = id;
        this.fullName = fullName;
        this.branch = branch;
    }

    // --- GETTER VE SETTERLAR ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}