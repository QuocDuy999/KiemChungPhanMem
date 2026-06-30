package com.example.food_store.messaging.producer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.example.food_store.constant.AppConstant;
import com.example.food_store.messaging.message.EmailRequest;

@ExtendWith(MockitoExtension.class)
class EmailProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailProducer emailProducer;

    @Test
    void sendEmailToQueue_ShouldSendMessageToRabbitMQ() {

        EmailRequest emailRequest = new EmailRequest(
                "test@gmail.com",
                "Test Subject",
                "Test Body"
        );

        emailProducer.sendEmailToQueue(emailRequest);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        AppConstant.EXCHANGE,
                        AppConstant.ROUTING_KEY,
                        emailRequest
                );
    }
}