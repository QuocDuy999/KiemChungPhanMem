package com.example.food_store.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.food_store.service.impl.UserService;
import static org.mockito.Mockito.*;

class SecurityConfigurationTest {

    private SecurityConfiguration securityConfiguration;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
      
        securityConfiguration = new SecurityConfiguration();
        userService = mock(UserService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userDetailsService = mock(UserDetailsService.class);
    }

    @Test
    void testPasswordEncoder_Bean_Logic() {

        PasswordEncoder encoder = securityConfiguration.passwordEncoder();
        
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testUserDetailsService_Bean_Creation() {
 
        UserDetailsService service = securityConfiguration.userDetailsService(userService);
        assertNotNull(service);
    }

    @Test
    void testAuthProvider_Configuration_Logic() {
  
        DaoAuthenticationProvider provider = securityConfiguration.authProvider(passwordEncoder, userDetailsService);
        
        assertNotNull(provider);
      
        assertThrows(Exception.class, () -> {
            provider.authenticate(null);
        });
    }

    @Test
    void testRememberMeServices_Bean_Logic() {
  
        assertNotNull(securityConfiguration.rememberMeServices());
    }

    @Test
    void testCustomSuccessHandler_Bean_Creation() {
  
        assertNotNull(securityConfiguration.customSuccessHandler(userService));
    }
}
