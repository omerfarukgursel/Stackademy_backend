package com.stackademy.proje.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String toEmail, String activationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SENIN_GMAIL_ADRESIN@gmail.com"); // Kimden gidiyor
        message.setTo(toEmail); // Kime gidiyor
        message.setSubject("Hesap Aktivasyon Kodu"); // Konu
        message.setText("Merhaba,\n\n" +
                "Hesabınızı doğrulamak için aşağıdaki kodu kullanın:\n\n" +
                activationCode + "\n\n" +
                "İyi çalışmalar dileriz."); // İçerik

        mailSender.send(message);
        System.out.println("Mail başarıyla gönderildi: " + toEmail);
    }

    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SENIN_GMAIL_ADRESIN@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Şifre Yenileme Kodu");
        message.setText("Merhaba,\n\n" +
                "Şifrenizi sıfırlamak için aşağıdaki kodu kullanın:\n\n" +
                resetCode + "\n\n" +
                "Bu işlemi siz yapmadıysanız bu maili dikkate almayınız.\n\n" +
                "İyi çalışmalar dileriz.");

        mailSender.send(message);
        System.out.println("Şifre sıfırlama maili gönderildi: " + toEmail);
    }

    public void sendFeedbackEmail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SENIN_GMAIL_ADRESIN@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Görüş/Öneri: " + subject);
        message.setText(content);

        mailSender.send(message);
        System.out.println("Feedback maili gönderildi: " + toEmail);
    }

    // Deneme sonucu email gönder
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SENIN_GMAIL_ADRESIN@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Email gönderildi: " + toEmail + " - " + subject);
    }
}