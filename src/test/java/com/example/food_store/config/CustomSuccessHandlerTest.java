package com.example.food_store.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.WebAttributes;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.service.impl.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class CustomSuccessHandlerTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomSuccessHandler successHandler;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");
        mockUser.setFullName("Test User");
        mockUser.setAvatar("avatar.png");
        
        Role role = new Role();
        role.setName("USER");
        mockUser.setRole(role);
        
        lenient().when(request.getContextPath()).thenReturn("");
        // Cấu hình cơ bản cho redirect
        lenient().when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ================= TEST determineTargetUrl =================

    @Test
    void determineTargetUrl_ShouldReturnAdmin_WhenRoleAdmin() {
        // Arrange
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        String targetUrl = successHandler.determineTargetUrl(authentication);

        // Assert
        assertEquals("/admin", targetUrl);
    }

    @Test
    void determineTargetUrl_ShouldReturnHome_WhenRoleUser() {
        // Arrange
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        String targetUrl = successHandler.determineTargetUrl(authentication);

        // Assert
        assertEquals("/", targetUrl);
    }

    @Test
    void determineTargetUrl_ShouldReturnHome_WhenNoMatchingRole() {
        // Arrange
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_UNKNOWN"));
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        String targetUrl = successHandler.determineTargetUrl(authentication);

        // Assert
        assertEquals("/", targetUrl);
    }

    // ================= TEST onAuthenticationSuccess =================

    @Test
    void onAuthenticationSuccess_ShouldNotRedirect_WhenResponseIsCommitted() throws IOException, ServletException {
        // Arrange
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(response.isCommitted()).thenReturn(true);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void onAuthenticationSuccess_ShouldRedirectAndSetSessionAttributes_WithCart() throws IOException, ServletException {
        // Arrange
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(response.isCommitted()).thenReturn(false);
        
        when(request.getSession(false)).thenReturn(session);
        when(authentication.getName()).thenReturn("test@gmail.com");
        when(userService.getUserByEmail("test@gmail.com")).thenReturn(mockUser);

        Cart cart = new Cart();
        cart.setSum(5);
        mockUser.setCart(cart);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect("/"); 
        verify(session).removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        
        verify(session).setAttribute("role", "USER");
        verify(session).setAttribute("fullName", "Test User");
        verify(session).setAttribute("avatar", "avatar.png");
        verify(session).setAttribute("id", 1L);
        verify(session).setAttribute("email", "test@gmail.com");
        verify(session).setAttribute("sum", 5);
    }

    @Test
    void onAuthenticationSuccess_ShouldSetSessionAttributes_WithoutCart() throws IOException, ServletException {
        // Arrange
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(response.isCommitted()).thenReturn(false);
        
        when(request.getSession(false)).thenReturn(session);
        when(authentication.getName()).thenReturn("test@gmail.com");
        when(userService.getUserByEmail("test@gmail.com")).thenReturn(mockUser);

        mockUser.setCart(null);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(session).setAttribute("sum", 0);
    }

    @Test
    void onAuthenticationSuccess_ShouldDoNothingWithSession_WhenSessionIsNull() throws IOException, ServletException {
        // Arrange
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(response.isCommitted()).thenReturn(false);
        
        when(request.getSession(false)).thenReturn(null);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(session, never()).removeAttribute(anyString());
        verify(userService, never()).getUserByEmail(anyString());
    }
}