package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {

        // Arrange
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        User user = new User();
        user.setEmail("admin@gmail.com");
        user.setPassword("123456");
        user.setRole(role);

        when(userService.getUserByEmail("admin@gmail.com"))
                .thenReturn(user);

        // Act
        UserDetails result =
                customUserDetailsService.loadUserByUsername("admin@gmail.com");

        // Assert
        assertNotNull(result);
        assertEquals("admin@gmail.com", result.getUsername());
        assertEquals("123456", result.getPassword());

        Collection<? extends GrantedAuthority> authorities =
                result.getAuthorities();

        assertEquals(1, authorities.size());

        GrantedAuthority authority =
                authorities.iterator().next();

        assertEquals("ROLE_ADMIN", authority.getAuthority());

        verify(userService, times(1))
                .getUserByEmail("admin@gmail.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {

        // Arrange
        when(userService.getUserByEmail("notfound@gmail.com"))
                .thenReturn(null);

        // Act + Assert
        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> customUserDetailsService.loadUserByUsername("notfound@gmail.com")
                );

        assertEquals("user not found", exception.getMessage());

        verify(userService, times(1))
                .getUserByEmail("notfound@gmail.com");
    }
        @Test
    void loadUserByUsername_ShouldReturnRoleUser_WhenRoleIsUser() {

        // Arrange
        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setEmail("user@gmail.com");
        user.setPassword("password123");
        user.setRole(role);

        when(userService.getUserByEmail("user@gmail.com"))
                .thenReturn(user);

        // Act
        UserDetails result =
                customUserDetailsService.loadUserByUsername("user@gmail.com");

        // Assert
        assertNotNull(result);
        assertEquals("user@gmail.com", result.getUsername());
        assertEquals("password123", result.getPassword());

        GrantedAuthority authority =
                result.getAuthorities().iterator().next();

        assertEquals("ROLE_USER", authority.getAuthority());

        verify(userService, times(1))
                .getUserByEmail("user@gmail.com");
    }

    @Test
    void loadUserByUsername_ShouldReturnSingleAuthority() {

        // Arrange
        Role role = new Role();
        role.setName("ROLE_MANAGER");

        User user = new User();
        user.setEmail("manager@gmail.com");
        user.setPassword("manager123");
        user.setRole(role);

        when(userService.getUserByEmail("manager@gmail.com"))
                .thenReturn(user);

        // Act
        UserDetails result =
                customUserDetailsService.loadUserByUsername("manager@gmail.com");

        // Assert
        assertNotNull(result);

        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());

        assertEquals(1, result.getAuthorities().size());

        GrantedAuthority authority =
                result.getAuthorities().iterator().next();

        assertEquals("ROLE_MANAGER", authority.getAuthority());

        verify(userService, times(1))
                .getUserByEmail("manager@gmail.com");
    }

}