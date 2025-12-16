package com.stackademy.proje.dto;

import java.util.Map;

public class SubmitQuizRequest {
    // Soru ID -> Cevap Şıkkı (A, B, C, D, E)
    private Map<String, String> answers;

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
}
