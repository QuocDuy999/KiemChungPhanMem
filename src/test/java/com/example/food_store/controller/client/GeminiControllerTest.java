package com.example.food_store.controller.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GeminiControllerTest {

    private GeminiController controller;

    @BeforeEach
    void setUp() throws Exception {

        controller = new GeminiController();

        Field apiUrl = GeminiController.class.getDeclaredField("apiUrl");
        apiUrl.setAccessible(true);

        // URL sai để ép controller đi vào catch(Exception)
        apiUrl.set(controller, "abc://invalid-url");
    }

    @Test
    void proxyToGemini_ShouldReturn500_WhenApiUrlInvalid() {

        ResponseEntity<String> response =
                controller.proxyToGemini("{\"contents\":[]}");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());

        assertTrue(response.getBody().contains("Không thể kết nối"));
    }

}