package com.example.food_store.controller.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.ChangePasswordDTO;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.service.impl.OrderService;
import com.example.food_store.service.impl.ProductService;
import com.example.food_store.service.impl.UploadService;
import com.example.food_store.service.impl.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomePageControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Mock
    private UploadService uploadService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailProducer emailProducer;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private HomePageController homePageController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setPassword("123");
    }

    // ================= HOME =================

    @Test
    void getHomePage_ShouldReturnView() {

        when(productService.fetchProductByType(anyString()))
                .thenReturn(new ArrayList<>());

        when(productService.fetchAllProductsToHomePage())
                .thenReturn(new ArrayList<>());

        when(productService.getAllNames())
                .thenReturn(List.of("A", "B"));

        String view = homePageController.getHomePage(model);

        assertEquals("client/homepage/show", view);

        verify(model).addAttribute(eq("products"), any());
        verify(model).addAttribute(eq("nameProducts"), any());
    }

    // ================= REGISTER =================

    @Test
    void getRegisterPage_ShouldReturnView() {
        assertEquals("client/auth/register",
                homePageController.getRegisterPage(model));
    }

    // ================= LOGIN =================

    @Test
    void getLoginPage_ShouldReturnView() {
        assertEquals("client/auth/login",
                homePageController.getLoginPage());
    }

    // ================= ORDER =================

    @Test
    void getOrderHistory_ShouldReturnView() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(orderService.fetchOrderByUser(any(User.class)))
                .thenReturn(new ArrayList<>());

        String view = homePageController.getOrderHistoryPage(model, request);

        assertEquals("client/cart/order-history", view);
    }

    // ================= PROFILE =================

    @Test
    void getProfileView_ShouldReturnView() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(userService.getUserById(1L)).thenReturn(user);

        String view = homePageController.getProfileView(request, model);

        assertEquals("client/homepage/viewProfile", view);
    }

    @Test
    void getProfileUpdate_ShouldReturnUpdatePage() {

        when(session.getAttribute("id")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        String view = homePageController.getProfileUpdate(session, model, 1L);

        assertEquals("client/homepage/updateProfile", view);

        verify(model).addAttribute("id", 1L);
        verify(model).addAttribute("newUser", user);
    }

    @Test
    void getProfileUpdate_ShouldReturnNotMatch_WhenIdDifferent() {

        when(session.getAttribute("id")).thenReturn(2L);

        String view = homePageController.getProfileUpdate(session, model, 1L);

        assertEquals("not-match", view);
    }

    @Test
    void getProfileUpdate_ShouldReturnNotMatch_WhenSessionNull() {

        when(session.getAttribute("id")).thenReturn(null);

        String view = homePageController.getProfileUpdate(session, model, 1L);

        assertEquals("not-match", view);
    }

    // ================= CHANGE PASSWORD =================

    @Test
    void changePassword_ShouldSuccess() {

        when(bindingResult.hasErrors()).thenReturn(false);

        when(userService.getUserById(1L)).thenReturn(user);

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed");

        ChangePasswordDTO dto = ChangePasswordDTO.builder()
                .userId(1L)
                .lastPassword("old")
                .newPassword("new")
                .build();

        String view = homePageController.changePassword(dto, bindingResult, model);

        assertEquals("redirect:/success-page", view);

        verify(userService).handleSaveUser(any(User.class));
    }

    @Test
    void changePassword_ShouldFail() {

        when(bindingResult.hasErrors()).thenReturn(false);

        when(userService.getUserById(1L)).thenReturn(user);

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        ChangePasswordDTO dto = ChangePasswordDTO.builder()
                .userId(1L)
                .lastPassword("wrong")
                .newPassword("new")
                .build();

        String view = homePageController.changePassword(dto, bindingResult, model);

        assertEquals("client/homepage/changePassword", view);

        verify(model).addAttribute("error", "Mật khẩu không chính xác");
    }

    @Test
    void changePassword_ShouldReturnValidationError() {

        when(bindingResult.hasErrors()).thenReturn(true);

        FieldError fieldError = new FieldError(
                "changePasswordDTO",
                "newPassword",
                "Invalid password");

        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ChangePasswordDTO dto = ChangePasswordDTO.builder()
                .userId(1L)
                .build();

        String view = homePageController.changePassword(dto, bindingResult, model);

        assertEquals("client/homepage/changePassword", view);

        verify(model).addAttribute("errorNewpassword", "Invalid password");
    }

    @Test
    void changePassword_ShouldReturnDefaultError_WhenFieldErrorNull() {

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldError()).thenReturn(null);

        ChangePasswordDTO dto = ChangePasswordDTO.builder()
                .userId(1L)
                .build();

        String view = homePageController.changePassword(dto, bindingResult, model);

        assertEquals("client/homepage/changePassword", view);

        verify(model).addAttribute("errorNewpassword", "Dữ liệu không hợp lệ");
    }
}