package com.example.food_store.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.net.URLEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import com.example.food_store.exception.CustomOAuth2Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomOAuth2AuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
        // Giả lập Context Path rỗng để tránh chuỗi "null/" khi DefaultRedirectStrategy hoạt động
        lenient().when(request.getContextPath()).thenReturn("");
        
        // Giả lập trả về đúng URL truyền vào khi qua hàm encode
        lenient().when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void onAuthenticationFailure_ShouldRedirectWithDefaultMessage_WhenGenericException() throws Exception {
        // Arrange
        // Dùng một ngoại lệ chung chung không phải CustomOAuth2Exception
        AuthenticationException exception = mock(AuthenticationException.class);
        
        String defaultMessage = "Đăng nhập thất bại. Vui lòng thử lại.";
        String expectedUrl = "/login?error1=" + URLEncoder.encode(defaultMessage, "UTF-8");

        // Act
        handler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(expectedUrl);
    }

    @Test
    void onAuthenticationFailure_ShouldRedirectWithCustomMessage_WhenCustomOAuth2Exception() throws Exception {
        // Arrange
        // Dùng đúng class ngoại lệ CustomOAuth2Exception của bạn
        CustomOAuth2Exception exception = mock(CustomOAuth2Exception.class);
        String customMessage = "Tài khoản đã tồn tại do đăng nhập bằng phương thức khác";
        
        when(exception.getMessage()).thenReturn(customMessage);
        
        String expectedUrl = "/login?error1=" + URLEncoder.encode(customMessage, "UTF-8");

        // Act
        handler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(expectedUrl);
    }
}
