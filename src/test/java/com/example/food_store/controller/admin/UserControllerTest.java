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
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import com.example.food_store.domain.User;
import com.example.food_store.domain.Token;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.dto.ResetPasswordDTO;
import com.example.food_store.messaging.message.EmailRequest;
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

    // ================= LIST / PAGINATION =================
    @Test
    void getUserPage_ShouldReturnView_WithPageSpecified() {
        Page<User> page = new PageImpl<>(java.util.List.of(user));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        String view = userController.getUserPage(model, Optional.of("1"));

        assertEquals("admin/user/show", view);
        verify(model).addAttribute(eq("listUser"), any());
        verify(model).addAttribute(eq("currentPage"), eq(1));
    }

    @Test
    void getUserPage_ShouldReturnView_WithEmptyPage_FallbackToPage1() {
        Page<User> pageData = new PageImpl<>(java.util.List.of(user));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageData);

        String view = userController.getUserPage(model, Optional.empty());

        assertEquals("admin/user/show", view);
        verify(model).addAttribute(eq("currentPage"), eq(1));
    }

    @Test
    void getUserPage_ShouldReturnNotMatch_WhenExceptionThrown() {
        when(userService.getAllUsers(any(Pageable.class))).thenThrow(new RuntimeException("DB error"));

        String view = userController.getUserPage(model, Optional.of("1"));

        assertEquals("not-match", view);
        verify(model).addAttribute("errorMessage", "Không tìm thấy trang .");
    }

    // ================= GET ADMIN PAGES =================
    @Test
    void getCreatePage_ShouldReturnView() {
        String view = userController.getCreateUserPage(model);
        assertEquals("admin/user/create", view);
        verify(model).addAttribute(eq("newUser"), any(User.class));
    }

    @Test
    void getDetailPage_ShouldReturnView() {
        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.getUserDetailPage(model, 1L);

        assertEquals("admin/user/detail", view);
        verify(model).addAttribute(eq("user"), eq(user));
        verify(model).addAttribute(eq("id"), eq(1L));
    }

    @Test
    void getUpdatePage_ShouldReturnView() {
        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.getUpdateUserPage(model, 1L);

        assertEquals("admin/user/update", view);
        verify(model).addAttribute(eq("newUser"), eq(user));
    }

    @Test
    void getDeleteUserPage_ShouldReturnView() {
        String view = userController.getDeleteUserPage(model, 1L);

        assertEquals("admin/user/delete", view);
        verify(model).addAttribute("id", 1L);
        verify(model).addAttribute(eq("newUser"), any(User.class));
    }

    // ================= POST ACTIONS =================
    @Test
    void createUser_ShouldRedirect_WhenValid() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.checkEmailExist(anyString())).thenReturn(false);
        when(uploadService.handleSaveUploadFile(any(MultipartFile.class), eq("avatar"))).thenReturn("img.png");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userService.getRoleByName(anyString())).thenReturn(user.getRole());

        String view = userController.createUser(model, user, bindingResult, file);

        assertEquals("redirect:/admin/user", view);
        verify(userService).handleSaveUser(any(User.class));
    }

    @Test
    void createUser_ShouldReturnCreateView_WhenBindingResultHasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.checkEmailExist(anyString())).thenReturn(false);

        String view = userController.createUser(model, user, bindingResult, file);

        assertEquals("admin/user/create", view);
        verify(userService, never()).handleSaveUser(any());
    }

    @Test
    void createUser_ShouldReturnCreateView_WhenEmailExists() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.checkEmailExist(anyString())).thenReturn(true);

        String view = userController.createUser(model, user, bindingResult, file);

        assertEquals("admin/user/create", view);
        verify(model).addAttribute("errorEmail", "Email đã tồn tại.");
        verify(userService, never()).handleSaveUser(any());
    }

    @Test
    void updateUser_ShouldRedirectAndSave_WhenUserFound() {
        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.postUpdateUser(model, user);

        assertEquals("redirect:/admin/user", view);
        verify(userService).handleSaveUser(any(User.class));
    }

    @Test
    void updateUser_ShouldRedirect_WhenUserNotFound() {
        when(userService.getUserById(1L)).thenReturn(null);

        String view = userController.postUpdateUser(model, user);

        assertEquals("redirect:/admin/user", view);
        verify(userService, never()).handleSaveUser(any());
    }

    @Test
    void deleteUser_ShouldRedirect() {
        String view = userController.postDeleteUser(model, user);

        assertEquals("redirect:/admin/user", view);
        verify(userService).deleteUserById(1L);
    }

    @Test
    void sendRequestToMail_ShouldSaveTokenAndSendEmail() {
        String view = userController.sendRequestToMail("test@gmail.com");

        assertEquals("redirect:/login", view);
        verify(tokenService).saveToken(any(Token.class));
        verify(emailProducer).sendEmailToQueue(any(EmailRequest.class));
    }

    // ================= RESET PASSWORD =================
    @Test
    void resetPasswordPage_ShouldReturnView() {
        when(tokenService.getEmailByToken(anyString())).thenReturn("test@gmail.com");
        when(userService.getUserByEmail(anyString())).thenReturn(user);

        String view = userController.getResetPasswordPage("token", model);

        assertEquals("client/homepage/resetPassword", view);
    }

    @Test
    void processResetPassword_ShouldSuccess_WhenPasswordsMatch() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setUserID(1L);
        dto.setNewPassword("123");
        dto.setConfirmPassword("123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        String view = userController.getProcessResetPassword(dto, bindingResult, model);

        assertEquals("client/homepage/resetPasswordSuccess", view);
        verify(userService).handleSaveUser(user);
    }

    @Test
    void processResetPassword_ShouldReturnError_WhenPasswordsDoNotMatch() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setUserID(1L);
        dto.setNewPassword("123");
        dto.setConfirmPassword("456");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);

        String view = userController.getProcessResetPassword(dto, bindingResult, model);

        assertEquals("client/homepage/resetPassword", view);
        verify(model).addAttribute("errorConfirmPassword", "Mật khẩu không khớp");
        verify(userService, never()).handleSaveUser(any());
    }

    @Test
    void processResetPassword_ShouldReturnError_WhenBindingResultHasErrors() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        when(bindingResult.hasErrors()).thenReturn(true);
        FieldError fieldError = new FieldError("dto", "newPassword", "Mật khẩu không hợp lệ");
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        String view = userController.getProcessResetPassword(dto, bindingResult, model);

        assertEquals("client/homepage/resetPassword", view);
        verify(model).addAttribute("errorNewpassword", "Mật khẩu không hợp lệ");
        verify(userService, never()).handleSaveUser(any());
    }
    
}