package com.example.food_store.controller.admin;

import com.example.food_store.controller.admin.UserController;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.service.impl.TokenService;
import com.example.food_store.service.impl.UploadService;
import com.example.food_store.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

class RegisterBusinessValidationTest {

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

    private UserController controller;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        controller = new UserController(
                userService,
                uploadService,
                passwordEncoder,
                tokenService,
                emailProducer
        );
    }

    @Test
    void testREG_TC_006_ConfirmPasswordMismatch() {

        // testcase confirm password
        // viết theo báo cáo
    }

    @Test
    void testREG_TC_008_EmailAlreadyExists() {

        when(userService.checkEmailExist("exist@gmail.com"))
                .thenReturn(true);

        verify(userService, never()).handleSaveUser(any());

    }

}