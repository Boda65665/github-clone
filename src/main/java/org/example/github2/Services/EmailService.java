package org.example.github2.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    private void sendMessage(String to, String subject, String messageString) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(messageString);
            emailSender.send(message);
        }
        catch (Exception | Error e){
            log.error(e.toString());
        }
    }

    public int sendCode(String to){
        int randomCode = generateRandomCode();
        sendMessage(to, "Код потверждения", "Your code is " + randomCode);
        return randomCode;
    }

    private int generateRandomCode() {
        Random random = new Random();

        int min = 100001;
        int max = 999999;
        return random.nextInt(max - min + 1) + min;
    }

    public String sendUrlForReSetPassword(String to){
        String key = generateRandomSecretKey();
        sendMessage(to, "Ссылка для востановления" ,"Your url: http://localhost:8080/auth/re-set-password/" + key);
        return key;
    }

    private String generateRandomSecretKey(){
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(30);
        for (int i = 0; i < 30; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
