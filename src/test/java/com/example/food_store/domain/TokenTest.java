package com.example.food_store.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenTest {

    private Token token;

    @BeforeEach
    void setUp() {
        token = new Token();
    }

    @Test
    void testGettersAndSetters() {
        token.setId(1L);
        token.setToken("REF123"); 
        token.setEmail("admin@gmail.com");

        assertEquals(1L, token.getId());
        assertEquals("REF123", token.getToken());
        assertEquals("admin@gmail.com", token.getEmail());
    }

    @Test
    void testAllArgsConstructorAndBuilder() {
        Token builtToken = Token.builder()
                .id(1L)
                .token("REF123")
                .email("admin@gmail.com")
                .build();

        assertNotNull(builtToken);
        assertEquals(1L, builtToken.getId());
        assertEquals("REF123", builtToken.getToken());
        assertEquals("admin@gmail.com", builtToken.getEmail());
    }
    @Test
    void testToken_Boundary_Fields_NullAndEmpty() {
        token.setToken("");
        token.setEmail(null);

        assertEquals("", token.getToken());
        assertNull(token.getEmail());
    }
    @Test
    void testToken_Boundary_InvalidID() {
        token.setId(-1L);
        assertEquals(-1L, token.getId());

        token.setId(0L);
        assertEquals(0L, token.getId());
    }
}
