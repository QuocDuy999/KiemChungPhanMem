package com.example.food_store.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.example.food_store.domain.User;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.dto.ResetPasswordDTO;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.service.impl.TokenService;
import com.example.food_store.service.impl.UploadService;
import com.example.food_store.service.impl.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UploadService uploadService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailProducer emailProducer;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setPassword("123");

        Role role = new Role();
        role.setName("USER");
        user.setRole(role);
    }

    // ================= LIST =================
    @Test
    void getUserPage_ShouldReturnView() {
        Page<User> page = new PageImpl<>(java.util.List.of(user));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        String view = userController.getUserPage(model, Optional.of("1"));

        assertEquals("admin/user/show", view);

        verify(model).addAttribute(eq("listUser"), any());
        verify(model).addAttribute(eq("currentPage"), eq(1));
    }

    // ================= CREATE PAGE =================
    @Test
    void getCreatePage_ShouldReturnView() {
        String view = userController.getCreateUserPage(model);

        assertEquals("admin/user/create", view);
        verify(model).addAttribute(eq("newUser"), any(User.class));
    }

    // ================= DETAIL =================
    @Test
    void getDetailPage_ShouldReturnView() {
        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.getUserDetailPage(model, 1L);

        assertEquals("admin/user/detail", view);

        verify(model).addAttribute(eq("user"), eq(user));
        verify(model).addAttribute(eq("id"), eq(1L));
    }

    // ================= UPDATE PAGE =================
    @Test
    void getUpdatePage_ShouldReturnView() {
        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.getUpdateUserPage(model, 1L);

        assertEquals("admin/user/update", view);

        verify(model).addAttribute(eq("newUser"), eq(user));
    }

    // ================= CREATE USER =================
    @Test
    void createUser_ShouldRedirect() {

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.checkEmailExist(anyString())).thenReturn(false);

        when(uploadService.handleSaveUploadFile(any(MultipartFile.class), eq("avatar")))
                .thenReturn("img.png");

        // FIX STRICT STUBBING (QUAN TRỌNG)
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        when(userService.getRoleByName(anyString())).thenReturn(user.getRole());

        String view = userController.createUser(model, user, bindingResult, file);

        assertEquals("redirect:/admin/user", view);

        verify(userService).handleSaveUser(any(User.class));
    }

    // ================= UPDATE =================
    @Test
    void updateUser_ShouldRedirect() {

        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.postUpdateUser(model, user);

        assertEquals("redirect:/admin/user", view);

        verify(userService).handleSaveUser(any(User.class));
    }

    // ================= DELETE =================
    @Test
    void deleteUser_ShouldRedirect() {

        String view = userController.postDeleteUser(model, user);

        assertEquals("redirect:/admin/user", view);

        verify(userService).deleteUserById(1L);
    }

    // ================= RESET PASSWORD =================
    @Test
    void resetPasswordPage_ShouldReturnView() {

        when(tokenService.getEmailByToken(anyString())).thenReturn("test@gmail.com");
        when(userService.getUserByEmail(anyString())).thenReturn(user);

        String view = userController.getResetPasswordPage("token", model);

        assertEquals("client/homepage/resetPassword", view);
    }

    // ================= PROCESS RESET PASSWORD =================
    @Test
    void processResetPassword_ShouldSuccess() {

        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setUserID(1L);
        dto.setNewPassword("123");
        dto.setConfirmPassword("123");

        when(userService.getUserById(1L)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        String view = userController.getProcessResetPassword(dto, bindingResult, model);

        assertEquals("client/homepage/resetPasswordSuccess", view);
    }
}