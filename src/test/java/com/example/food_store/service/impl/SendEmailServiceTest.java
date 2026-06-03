package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.food_store.constant.AppConstant;

@ExtendWith(MockitoExtension.class)
class SendEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SendEmailService sendEmailService;

    @Test
    void testSendEmail() {

        String toEmail = "cloud@gmail.com";
        String subject = "Test Subject";
        String body = "Hello Cloud";

        sendEmailService.sendEmail(
                toEmail,
                subject,
                body);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(
                        SimpleMailMessage.class);

        verify(mailSender)
                .send(captor.capture());

        SimpleMailMessage message =
                captor.getValue();

        assertEquals(
                AppConstant.SYSTEM_EMAIL_SENDER,
                message.getFrom());

        assertEquals(
                toEmail,
                message.getTo()[0]);

        assertEquals(
                subject,
                message.getSubject());

        assertEquals(
                body,
                message.getText());
    }
}