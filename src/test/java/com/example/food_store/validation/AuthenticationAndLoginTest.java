package com.example.food_store.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.service.impl.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAndLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean(name = "userDetailsService")
    private UserDetailsService userDetailsService;

    @MockBean
    private UserService userService;

    // Các MockBean ngoại vi ngăn sập ApplicationContext
    @MockBean
    private com.example.food_store.config.CustomOAuth2AuthenticationFailureHandler customFailureHandler;

    @MockBean
    private com.example.food_store.messaging.producer.EmailProducer emailProducer;

    @MockBean
    private com.example.food_store.service.impl.TokenService tokenService;

    @MockBean
    private com.example.food_store.service.impl.UploadService uploadService;

    private User adminUser;
    private User clientUser;

    @BeforeEach
    void setup() {
        // 1. Tạo Mock Data cho Admin User
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@gmail.com");
        adminUser.setFullName("Trần Thanh Hiển - Admin");
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setRole(adminRole);

        // 2. Tạo Mock Data cho Client User
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        clientUser = new User();
        clientUser.setId(2L);
        clientUser.setEmail("hien@gmail.com");
        clientUser.setFullName("Trần Thanh Hiển - Khách");
        clientUser.setPassword(passwordEncoder.encode("123456"));
        clientUser.setRole(userRole);
        
        Cart cart = new Cart();
        cart.setSum(5);
        clientUser.setCart(cart);
    }

    @Test
    public void testLOG_TC_001_AdminLoginSuccess() throws Exception {
        UserDetails adminDetails = new org.springframework.security.core.userdetails.User(
                adminUser.getEmail(),
                adminUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(userDetailsService.loadUserByUsername("admin@gmail.com")).thenReturn(adminDetails);
        when(userService.getUserByEmail("admin@gmail.com")).thenReturn(adminUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "admin@gmail.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername("admin@gmail.com"));
    }

    @Test
    public void testLOG_TC_002_ClientLoginSuccess() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);
        when(userService.getUserByEmail("hien@gmail.com")).thenReturn(clientUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "hien@gmail.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("hien@gmail.com"));
    }

    @Test
    public void testLOG_TC_003_LoginFailed_WrongPassword() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);

        mockMvc.perform(formLogin("/login")
                .user("username", "hien@gmail.com")
                .password("password", "SaiMatKhau999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_004_LoginFailed_EmailNotFound() throws Exception {
        when(userDetailsService.loadUserByUsername("ghost@gmail.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(formLogin("/login")
                .user("username", "ghost@gmail.com")
                .password("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_005_LoginEmptyFields() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("username", "")
                .password("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_006_LoginInvalidEmailFormat() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("username", "hien.tran_at_gmail")
                .password("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_007_LoginCaseSensitivePassword() throws Exception {
        User caseUser = new User();
        caseUser.setEmail("case@gmail.com");
        caseUser.setPassword(passwordEncoder.encode("PassWord123"));

        UserDetails caseDetails = new org.springframework.security.core.userdetails.User(
                caseUser.getEmail(),
                caseUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("case@gmail.com")).thenReturn(caseDetails);

        mockMvc.perform(formLogin("/login")
                .user("username", "case@gmail.com")
                .password("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_008_LogoutSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ROLE_USER");
        session.setAttribute("email", "hien@gmail.com");

        mockMvc.perform(post("/logout").session(session).with(user("hien@gmail.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_009_LoginMinMinusOneBoundary() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);

        mockMvc.perform(formLogin("/login")
                .user("username", "hien@gmail.com")
                .password("password", "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_010_LoginMinBoundary() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);
        when(userService.getUserByEmail("hien@gmail.com")).thenReturn(clientUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "hien@gmail.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());
    }

    @Test
    public void testLOG_TC_011_LoginMaxBoundary() throws Exception {
        String maxPassword = "A".repeat(255);
        
        User maxUser = new User();
        maxUser.setId(3L);
        maxUser.setEmail("maxpass@gmail.com");
        maxUser.setFullName("User Max Pass");
        maxUser.setPassword(passwordEncoder.encode(maxPassword));
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        maxUser.setRole(userRole);

        UserDetails maxUserDetails = new org.springframework.security.core.userdetails.User(
                maxUser.getEmail(),
                maxUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("maxpass@gmail.com")).thenReturn(maxUserDetails);
        when(userService.getUserByEmail("maxpass@gmail.com")).thenReturn(maxUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "maxpass@gmail.com")
                .param("password", maxPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("maxpass@gmail.com"));
    }
}