package com.stackademy.proje.dto;

import java.util.Map;

public class SubmitQuizRequest {
    private Map<Integer, String> answers; // {1: "A", 2: "C", 3: "B", ...}

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
    }
}
