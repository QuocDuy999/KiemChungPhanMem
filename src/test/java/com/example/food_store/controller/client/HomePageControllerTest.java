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
import org.springframework.web.multipart.MultipartFile;

import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.ChangePasswordDTO;
import com.example.food_store.domain.dto.RegisterDTO;
import com.example.food_store.messaging.message.EmailRequest;
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

    // ================= GET PAGES (SIMPLE MAPINGS) =================

    @Test
    void getHomePage_ShouldReturnView() {
        when(productService.fetchProductByType(anyString())).thenReturn(new ArrayList<>());
        when(productService.fetchAllProductsToHomePage()).thenReturn(new ArrayList<>());
        when(productService.getAllNames()).thenReturn(List.of("A", "B"));

        String view = homePageController.getHomePage(model);

        assertEquals("client/homepage/show", view);
        verify(model).addAttribute(eq("products"), any());
        verify(model).addAttribute(eq("nameProducts"), any());
    }

    @Test
    void getRegisterPage_ShouldReturnView() {
        assertEquals("client/auth/register", homePageController.getRegisterPage(model));
    }

    @Test
    void getLoginPage_ShouldReturnView() {
        assertEquals("client/auth/login", homePageController.getLoginPage());
    }

    @Test
    void getForgotPasswordPage_ShouldReturnView() {
        assertEquals("client/auth/password", homePageController.getForgotPasswordPage());
    }

    @Test
    void getDenyPage_ShouldReturnView() {
        assertEquals("client/auth/deny", homePageController.getDenyPage(model));
    }

    @Test
    void getSuccessPage_ShouldReturnView() {
        assertEquals("client/homepage/changePasswordSuccess", homePageController.getSuccessPage());
    }

    @Test
    void getChangePasswordPage_ShouldReturnView() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        String view = homePageController.getChangePasswordPage(request, model);

        assertEquals("client/homepage/changePassword", view);
        verify(model).addAttribute(eq("changePasswordDTO"), any(ChangePasswordDTO.class));
    }

    // ================= ORDER =================

    @Test
    void getOrderHistory_ShouldReturnView() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);
        when(orderService.fetchOrderByUser(any(User.class))).thenReturn(new ArrayList<>());

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

    @Test
    void updateProfile_PostMethod_ShouldUpdateAndRedirect() {
        when(request.getSession(false)).thenReturn(session);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);
        
        MultipartFile file = mock(MultipartFile.class);
        when(uploadService.handleSaveUploadFile(file, "avatar")).thenReturn("new-avatar.png");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setPhone("0123456789");
        updatedUser.setAddress("Hanoi");

        String view = homePageController.postMethodName(model, updatedUser, bindingResult, file, request);

        assertEquals("redirect:/view-profile", view);
        verify(userService).handleSaveUser(any(User.class));
        verify(session).setAttribute("avatar", "new-avatar.png");
    }

    @Test
    void updateProfile_PostMethod_ShouldReturnNotMatch_WhenHasErrors() {
        when(request.getSession(false)).thenReturn(session);
        when(userService.getUserById(1L)).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(true);

        User updatedUser = new User();
        updatedUser.setId(1L);
        MultipartFile file = mock(MultipartFile.class);

        String view = homePageController.postMethodName(model, updatedUser, bindingResult, file, request);

        assertEquals("not-match", view);
    }

    // ================= REGISTER POST (/register) =================

    @Test
    void handleRegister_ShouldReturnSuccess_WhenOTPIsCorrect() {
        RegisterDTO dto = new RegisterDTO();
        dto.setOTP("123456");

        when(userService.registerDTOtoUser(dto)).thenReturn(user);

        String view = homePageController.handleRegister(dto, bindingResult, "123456", model);

        assertEquals("client/homepage/registerSuccess", view);
        verify(userService).handleSaveUser(user);
    }

    @Test
    void handleRegister_ShouldReturnVerifyEmail_WhenOTPIsIncorrect() {
        RegisterDTO dto = new RegisterDTO();
        dto.setOTP("123456");

        String view = homePageController.handleRegister(dto, bindingResult, "654321", model);

        assertEquals("client/auth/verifyEmail", view);
        verify(model).addAttribute("errorVerifyEmail", "Mã OTP không chính xác. Vui lòng nhập lại.");
        verify(userService, never()).handleSaveUser(any(User.class));
    }

    // ================= VERIFY EMAIL POST (/verify) =================

    @Test
    void getVerifyPage_ShouldSendEmail_WhenNoErrors() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@gmail.com");
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = homePageController.getVerifyPage(dto, bindingResult, model);

        assertEquals("client/auth/verifyEmail", view);
        verify(emailProducer).sendEmailToQueue(any(EmailRequest.class));
        verify(model).addAttribute(eq("userDTO"), any(RegisterDTO.class));
    }

    @Test
    void getVerifyPage_ShouldReturnRegisterView_WhenHasErrors() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("invalid-email");
        dto.setFullName("Ab"); // < 3
        dto.setPassword("123"); // < 6
        dto.setConfirmPassword("1234"); // Mismatch
        
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.checkEmailExist("invalid-email")).thenReturn(true);

        String view = homePageController.getVerifyPage(dto, bindingResult, model);

        assertEquals("client/auth/register", view);
        verify(model).addAttribute("errorFullname", "Họ tên phải có tối thiểu 3 ký tự");
        verify(model).addAttribute("errorEmail", "Email đã tồn tại. Vui lòng sử dụng Email khác");
        verify(model).addAttribute("errorEmail_2", "Email không hợp lệ");
        verify(model).addAttribute("errorPassword", "Mật khẩu phải có tối thiểu 6 ký tự");
        verify(emailProducer, never()).sendEmailToQueue(any());
    }

    // ================= CHANGE PASSWORD =================

    @Test
    void changePassword_ShouldSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

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
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

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
        FieldError fieldError = new FieldError("changePasswordDTO", "newPassword", "Invalid password");
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ChangePasswordDTO dto = ChangePasswordDTO.builder().userId(1L).build();
        String view = homePageController.changePassword(dto, bindingResult, model);

        assertEquals("client/homepage/changePassword", view);
        verify(model).addAttribute("errorNewpassword", "Invalid password");
    }

    @Test
    void changePassword_ShouldReturnDefaultError_WhenFieldErrorNull() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldError()).thenReturn(null);

        ChangePasswordDTO dto = ChangePasswordDTO.builder().userId(1L).build();
        String view = homePageController.changePassword(dto, bindingResult, model);

        assertEquals("client/homepage/changePassword", view);
        verify(model).addAttribute("errorNewpassword", "Dữ liệu không hợp lệ");
    }
}