package com.bulbul.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService  {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendSimpleMail(String to, String subject, String message)
    {

        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(to);
            mailMessage.setText(message);
            mailMessage.setSubject(subject);

            // Sending the mail
            javaMailSender.send(mailMessage);
            log.info("Email sent successfully");
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            log.info("Error while sending email: " + e.getMessage());
        }
    }
}
