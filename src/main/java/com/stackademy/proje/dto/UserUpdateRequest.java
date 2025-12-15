package com.stackademy.proje.dto;

public class UserUpdateRequest {
    
    private String fullName;
    private String phone;
    private String schoolLevel; // Sınıf seviyesi (Örn: "12. Sınıf")

    // --- MANUEL GETTER VE SETTERLAR ---

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }



    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSchoolLevel() { return schoolLevel; }
    public void setSchoolLevel(String schoolLevel) { this.schoolLevel = schoolLevel; }
}