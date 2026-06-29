package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.food_store.domain.Token;
import com.example.food_store.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void getEmailByToken_ShouldReturnEmail_WhenTokenExists() {

        String token = "abc123";
        String email = "user@gmail.com";

        when(tokenRepository.findEmailByToken(token))
                .thenReturn(email);

        String result = tokenService.getEmailByToken(token);

        assertEquals(email, result);

        verify(tokenRepository, times(1))
                .findEmailByToken(token);
    }

    @Test
    void getEmailByToken_ShouldReturnNull_WhenTokenNotFound() {

        String token = "invalid-token";

        when(tokenRepository.findEmailByToken(token))
                .thenReturn(null);

        String result = tokenService.getEmailByToken(token);

        assertEquals(null, result);

        verify(tokenRepository, times(1))
                .findEmailByToken(token);
    }

    @Test
    void saveToken_ShouldCallRepositorySave() {

        Token token = Token.builder()
                .token("abc123")
                .email("user@gmail.com")
                .build();

        tokenService.saveToken(token);

        verify(tokenRepository, times(1))
                .save(token);
    }

    @Test
    void saveToken_ShouldSaveCorrectToken() {

        Token token = Token.builder()
                .token("reset-password-token")
                .email("admin@gmail.com")
                .build();

        tokenService.saveToken(token);

        verify(tokenRepository).save(token);
    }

}