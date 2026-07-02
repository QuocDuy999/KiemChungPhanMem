package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.food_store.config.TestConfig;
import com.example.food_store.domain.Token;

@DataJpaTest
@Import(TestConfig.class)
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    private Token tokenEntity;

    @BeforeEach
    void setUp() {
        tokenEntity = new Token();
        tokenEntity.setToken("REF123"); 
        tokenEntity.setEmail("admin@gmail.com"); 
        
        tokenEntity = tokenRepository.save(tokenEntity);
    }

    @Test
    void testFindEmailByToken_Found() {

        String resultEmail = tokenRepository.findEmailByToken("REF123");

        assertNotNull(resultEmail);
        assertEquals("admin@gmail.com", resultEmail);
    }

    @Test
    void testFindEmailByToken_NotFound() {

        String resultEmail = tokenRepository.findEmailByToken("NOT_FOUND_TOKEN");

        assertNull(resultEmail);
    }

    @Test
    void testSaveToken_Success() {

        Token newToken = new Token();
        newUserToken.setToken("REF456");
        newUserToken.setEmail("other_admin@gmail.com");

        Token savedToken = tokenRepository.save(newToken);

        assertNotNull(savedToken);
        assertNotEquals(0, savedToken.getId());
        assertEquals("REF456", savedToken.getToken());
        assertEquals("other_admin@gmail.com", savedToken.getEmail());
    }

    @Test
    void testFindEmailByToken_Boundary_NullAndEmpty() {

        String nullResult = tokenRepository.findEmailByToken(null);
        assertNull(nullResult);

        String emptyResult = tokenRepository.findEmailByToken("");
        assertNull(emptyResult);
    }
}
