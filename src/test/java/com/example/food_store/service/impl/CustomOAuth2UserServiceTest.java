package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.food_store.constant.OAuth2ProviderConstant;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.exception.CustomOAuth2Exception;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @InjectMocks
    private CustomOAuth2UserService service;

    private OAuth2User oauthUser;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("USER");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "user@gmail.com");
        attributes.put("name", "Nguyen Van A");

        oauthUser = new DefaultOAuth2User(
                java.util.Collections.emptyList(),
                attributes,
                "email");

        when(userRequest.getClientRegistration())
                .thenReturn(clientRegistration);

        // Đặt default là "google", sẽ ghi đè trong test của github
        lenient().when(clientRegistration.getRegistrationId())
                .thenReturn("google");

        when(userService.getRoleByName("USER"))
                .thenReturn(role);
    }

    @Test
    void processOAuth2User_ShouldCreateNewUser_WhenUserNotExists() {
        // Arrange
        when(userService.getUserByEmail("user@gmail.com")).thenReturn(null);

        // Act
        OAuth2User result = service.processOAuth2User(userRequest, oauthUser);

        // Assert
        assertNotNull(result);
        verify(userService).saveUser(any(User.class));
    }

    @Test
    void processOAuth2User_ShouldThrowException_WhenProviderDifferent() {
        // Arrange
        User user = new User();
        user.setProvider(OAuth2ProviderConstant.GITHUB);
        when(userService.getUserByEmail("user@gmail.com")).thenReturn(user);

        // Act & Assert
        assertThrows(CustomOAuth2Exception.class,
                () -> service.processOAuth2User(userRequest, oauthUser));

        verify(userService, never()).saveUser(any());
    }

    @Test
    void processOAuth2User_ShouldReturnOAuthUser_WhenProviderSame() {
        // Arrange
        User user = new User();
        user.setProvider(OAuth2ProviderConstant.GOOGLE);
        when(userService.getUserByEmail("user@gmail.com")).thenReturn(user);

        // Act
        OAuth2User result = service.processOAuth2User(userRequest, oauthUser);

        // Assert
        assertNotNull(result);
        verify(userService, never()).saveUser(any());
    }

    @Test
    void processOAuth2User_ShouldUseGithubAvatar_WhenRegisterFromGithub() {
        // Arrange
        when(clientRegistration.getRegistrationId()).thenReturn("github");
        when(userService.getUserByEmail("user@gmail.com")).thenReturn(null);

        // Act
        service.processOAuth2User(userRequest, oauthUser);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(captor.capture());

        assertEquals("default-github.png", captor.getValue().getAvatar());
        assertEquals(OAuth2ProviderConstant.GITHUB, captor.getValue().getProvider());
    }
}