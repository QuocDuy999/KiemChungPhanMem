package com.example.food_store.controller.admin;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.service.impl.TokenService;
import com.example.food_store.service.impl.UploadService;
import com.example.food_store.service.impl.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerRegisterTest {

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

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

    }

    // ===================================================
    // TC08 - Email đã tồn tại
    // ===================================================
    @Test
    void testREG_TC_008_EmailAlreadyExists() throws Exception {

        MockMultipartFile avatar = new MockMultipartFile(
                "avatarFile",
                "avatar.jpg",
                "image/jpeg",
                "image".getBytes());

        when(userService.checkEmailExist("exist@gmail.com"))
                .thenReturn(true);

        mockMvc.perform(

                multipart("/admin/user/create")

                        .file(avatar)

                        .param("fullName", "Nguyễn Văn A")

                        .param("email", "exist@gmail.com")

                        .param("password", "123456")

        )

                .andExpect(status().isOk())

                .andExpect(view().name("admin/user/create"))

                .andExpect(model().attributeExists("errorEmail"));

    }

    // ===================================================
    // TC12 - XSS
    // ===================================================
    @Test
    void testREG_TC_012_XSSInjection() throws Exception {

        MockMultipartFile avatar = new MockMultipartFile(
                "avatarFile",
                "avatar.jpg",
                "image/jpeg",
                "image".getBytes());

        Role role = new Role();
        role.setName("ROLE_USER");

        when(userService.checkEmailExist(anyString()))
                .thenReturn(false);

        when(uploadService.handleSaveUploadFile(any(), anyString()))
                .thenReturn("avatar.jpg");

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed");

        when(userService.getRoleByName(anyString()))
                .thenReturn(role);

        String payload =
                "<script>alert('Hack')</script>";

        mockMvc.perform(

                multipart("/admin/user/create")

                        .file(avatar)

                        .param("fullName", payload)

                        .param("email", "abc@gmail.com")

                        .param("password", "123456")

                        .param("role.name", "ROLE_USER")

        )

                .andExpect(status().is3xxRedirection());

        verify(userService).handleSaveUser(any(User.class));

    }

}