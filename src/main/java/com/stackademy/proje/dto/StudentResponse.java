package com.stackademy.proje.dto;

public class StudentResponse {

    private String id;
    private String fullName;
    private String subscriptionPlan;
    private String schoolLevel;

    // --- CONSTRUCTOR ---
    public StudentResponse(String id, String fullName, String subscriptionPlan, String schoolLevel) {
        this.id = id;
        this.fullName = fullName;
        this.subscriptionPlan = subscriptionPlan;
        this.schoolLevel = schoolLevel;
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

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public String getSchoolLevel() {
        return schoolLevel;
    }

    public void setSchoolLevel(String schoolLevel) {
        this.schoolLevel = schoolLevel;
    }
}