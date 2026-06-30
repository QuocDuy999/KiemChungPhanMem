package com.example.food_store.messaging.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmailRequestTest {

    @Test
    void noArgsConstructor_ShouldCreateEmptyObject() {

        EmailRequest request = new EmailRequest();

        assertNull(request.getToEmail());
        assertNull(request.getSubject());
        assertNull(request.getBody());
    }

    @Test
    void allArgsConstructor_ShouldSetFields() {

        EmailRequest request = new EmailRequest(
                "test@gmail.com",
                "Test Subject",
                "Test Body"
        );

        assertEquals("test@gmail.com", request.getToEmail());
        assertEquals("Test Subject", request.getSubject());
        assertEquals("Test Body", request.getBody());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {

        EmailRequest request = new EmailRequest();

        request.setToEmail("abc@gmail.com");
        request.setSubject("Hello");
        request.setBody("This is body");

        assertEquals("abc@gmail.com", request.getToEmail());
        assertEquals("Hello", request.getSubject());
        assertEquals("This is body", request.getBody());
    }

    @Test
    void toString_ShouldReturnExpectedValue() {

        EmailRequest request = new EmailRequest(
                "test@gmail.com",
                "Subject",
                "Body"
        );

        String expected = "EmailRequest{to='test@gmail.com', subject='Subject'}";

        assertEquals(expected, request.toString());
    }

    @Test
    void equalsAndHashCode_ShouldWork() {

        EmailRequest request1 = new EmailRequest(
                "a@gmail.com",
                "Subject",
                "Body"
        );

        EmailRequest request2 = new EmailRequest(
                "a@gmail.com",
                "Subject",
                "Body"
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}