package com.example.food_store.messaging.consumer;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.food_store.messaging.message.EmailRequest;
import com.example.food_store.service.impl.SendEmailService;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @Mock
    private SendEmailService sendEmail;

    @InjectMocks
    private EmailConsumer emailConsumer;

    private EmailRequest emailRequest;

    @BeforeEach
    void setUp() {

        emailRequest = new EmailRequest();
        emailRequest.setToEmail("test@gmail.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setBody("Test Body");

    }

    @Test
    void receiveEmailMessage_ShouldCallSendEmailService() {

        emailConsumer.receiveEmailMessage(emailRequest);

        verify(sendEmail, times(1)).sendEmail(
                "test@gmail.com",
                "Test Subject",
                "Test Body"
        );

    }

}