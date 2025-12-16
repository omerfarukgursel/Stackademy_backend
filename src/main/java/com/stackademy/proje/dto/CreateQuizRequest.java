package com.stackademy.proje.dto;

import java.util.List;

public class CreateQuizRequest {
    private String title;
    private String category;
    private String topic;
    private List<QuestionData> questions;

    // Nested class for question data
    public static class QuestionData {
        private int questionNumber;
        private String imageUrl;
        private String correctAnswer;

        public int getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(int questionNumber) {
            this.questionNumber = questionNumber;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<QuestionData> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionData> questions) {
        this.questions = questions;
    }
}
