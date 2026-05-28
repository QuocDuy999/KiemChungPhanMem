package com.example.food_store.service.impl;

import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.food_store.constant.AppConstant;
import com.example.food_store.service.ISendEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SendEmailService implements ISendEmailService {
    
    // CHỖ SỬA: Thêm từ khóa final ở đây
    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(AppConstant.SYSTEM_EMAIL_SENDER);
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        
        // Bây giờ mailSender đã được tiêm giá trị, không còn lo bị null nữa!
        mailSender.send(message);
    }
}