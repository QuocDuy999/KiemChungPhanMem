package com.example.food_store.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.RegisterDTO;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;

/**
 * Test LUỒNG ĐĂNG KÝ đầy đủ (end-to-end), đi qua toàn bộ Spring stack thật
 * (Controller thật -> Bean Validation thật -> UserService thật -> UserRepository thật -> DB thật).
 *
 * Luồng được mô phỏng đúng như người dùng thật thao tác:
 *   B1: POST /verify  -> hệ thống sinh mã OTP (email gửi qua RabbitMQ được @MockBean để không cần cài RabbitMQ)
 *   B2: POST /register kèm đúng OTP vừa sinh -> tài khoản được tạo THẬT trong DB
 *   B3: Kiểm tra dữ liệu thật: mật khẩu đã băm, role = ROLE_USER
 *
 * Yêu cầu môi trường: giống các test @SpringBootTest sẵn có trong project (AuthenticationAndLoginTest,
 * ProductCreateFlowTest...) - cần có MySQL đang chạy đúng theo cấu hình application.yml.
 * KHÔNG cần RabbitMQ vì EmailProducer đã được @MockBean.
 *
 * @Transactional giúp tự động rollback dữ liệu sau mỗi test -> không làm bẩn DB thật của bạn.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegisterFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailProducer emailProducer; // Không cần RabbitMQ thật khi test

    @BeforeEach
    void ensureRoleUserExists() {
        // registerDTOtoUser() tìm role theo đúng tên "ROLE_USER" (xem UserService.java) -> phải tồn tại trước.
        roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Vai trò người dùng thông thường");
            return roleRepository.save(r);
        });
    }

    @Test
    void fullRegisterFlow_CorrectOTP_ShouldCreateUserInDatabase() throws Exception {
        String email = "flow_register_" + System.currentTimeMillis() + "@gmail.com";

        // ===== B1: Gửi thông tin đăng ký lần đầu -> hệ thống sinh OTP và trả về trang xác thực =====
        MvcResult verifyResult = mockMvc.perform(post("/verify")
                        .param("fullName", "Nguyen Van Flow")
                        .param("email", email)
                        .param("password", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/auth/verifyEmail"))
                .andReturn();

        verify(emailProducer, times(1)).sendEmailToQueue(any());

        ModelMap model = verifyResult.getModelAndView().getModelMap();
        RegisterDTO userDTO = (RegisterDTO) model.get("userDTO");
        assertNotNull(userDTO, "Model phải chứa userDTO kèm OTP vừa sinh");
        String realOtp = userDTO.getOTP();
        assertNotNull(realOtp, "Hệ thống phải sinh ra mã OTP");

        // ===== B2: Xác nhận đúng OTP -> tạo tài khoản thật =====
        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyen Van Flow")
                        .param("email", email)
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("OTP", realOtp)
                        .param("OTP_check", realOtp))
                .andExpect(status().isOk())
                .andExpect(view().name("client/homepage/registerSuccess"));

        // ===== B3: Kiểm tra dữ liệu THẬT trong DB sau khi hoàn tất luồng =====
        User saved = userRepository.findByEmail(email);
        assertNotNull(saved, "Tài khoản phải được lưu thật trong DB sau khi hoàn tất luồng đăng ký");
        assertNotNull(saved.getRole(), "Role phải được gán (kiểm tra bảng 'roles' đã có ROLE_USER chưa)");
        assertEquals("ROLE_USER", saved.getRole().getName());
        assertNotEquals("123456", saved.getPassword(), "Mật khẩu phải được băm (hash), không được lưu plaintext");
        assertTrue(passwordEncoder.matches("123456", saved.getPassword()),
                "Mật khẩu đã băm phải khớp lại được với mật khẩu gốc bằng PasswordEncoder");
    }

    @Test
    void fullRegisterFlow_WrongOTP_ShouldNotCreateUser() throws Exception {
        String email = "flow_register_wrongotp_" + System.currentTimeMillis() + "@gmail.com";

        // B1: Gửi thông tin đăng ký -> nhận OTP thật (nhưng test này cố tình nhập sai ở B2)
        mockMvc.perform(post("/verify")
                        .param("fullName", "Nguyen Van Sai OTP")
                        .param("email", email)
                        .param("password", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().isOk());

        // B2: Nhập sai OTP
        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyen Van Sai OTP")
                        .param("email", email)
                        .param("password", "123456")
                        .param("confirmPassword", "123456")
                        .param("OTP", "111111")
                        .param("OTP_check", "999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/auth/verifyEmail"))
                .andExpect(model().attributeExists("errorVerifyEmail"));

        // B3: Xác nhận KHÔNG có tài khoản nào được tạo khi OTP sai
        assertNull(userRepository.findByEmail(email), "Không được tạo tài khoản khi OTP nhập sai");
    }
}