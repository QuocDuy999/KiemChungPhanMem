package com.example.food_store.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ResetPasswordRequestMatcherTest {

    private ResetPasswordRequestMatcher matcher;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        matcher = new ResetPasswordRequestMatcher();
    }

    @Test
    void matches_ShouldReturnTrue_WhenUriContainsResetPassword() {
        // Trường hợp URI trùng khớp hoàn toàn
        when(request.getRequestURI()).thenReturn("/reset-password");
        assertTrue(matcher.matches(request));

        // Trường hợp URI có chứa chuỗi ở giữa hoặc cuối
        when(request.getRequestURI()).thenReturn("/api/v1/reset-password/token-123");
        assertTrue(matcher.matches(request));
    }

    @Test
    void matches_ShouldReturnFalse_WhenUriDoesNotContainResetPassword() {
        // Trường hợp URI hoàn toàn khác
        when(request.getRequestURI()).thenReturn("/login");
        assertFalse(matcher.matches(request));

        // Trường hợp URI gần giống nhưng không khớp chính xác
        when(request.getRequestURI()).thenReturn("/reset");
        assertFalse(matcher.matches(request));
        
        when(request.getRequestURI()).thenReturn("/password-forgot");
        assertFalse(matcher.matches(request));
    }
}