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
}