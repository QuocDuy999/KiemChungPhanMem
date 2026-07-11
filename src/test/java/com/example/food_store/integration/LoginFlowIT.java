package com.example.food_store.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;

import jakarta.servlet.http.Cookie;

/**
 * Test LUỒNG ĐĂNG NHẬP đầy đủ (end-to-end), đi qua toàn bộ Spring Security filter chain thật
 * (không mock UserDetailsService như AuthenticationAndLoginTest - dùng CustomUserDetailsService
 * + DaoAuthenticationProvider + PasswordEncoder + CustomSuccessHandler đều chạy thật với DB thật).
 *
 * QUAN TRỌNG: project cấu hình spring.session.store-type=jdbc (Spring Session lưu qua MySQL),
 * nên phiên đăng nhập KHÔNG thể lấy qua request.getSession() kiểu thông thường như
 * MockHttpSession - Spring Session tự quản lý session riêng và trả về qua cookie "SESSION".
 * Vì vậy toàn bộ file này theo dõi phiên đăng nhập bằng cách lấy cookie "SESSION" từ response
 * sau khi login, rồi đính kèm cookie đó (.cookie(sessionCookie)) vào các request tiếp theo -
 * đây là cách làm đúng và duy nhất hoạt động được khi Spring Session JDBC đang bật.
 *
 * Luồng:
 *   B1: Đăng nhập đúng thông tin -> lấy cookie SESSION thật do Spring Session cấp
 *   B2: Dùng cookie đó truy cập 1 trang yêu cầu đăng nhập (/view-profile) -> phải thành công (200)
 *       (trang này đọc session.getAttribute("id") do CustomSuccessHandler set - nếu 200 tức là
 *       phiên đăng nhập & CustomSuccessHandler hoạt động đúng, không cần assert riêng attribute)
 *   B3: Đăng xuất
 *   B4: Truy cập lại trang yêu cầu đăng nhập (không còn cookie hợp lệ) -> phải bị từ chối (redirect /login)
 *
 * Yêu cầu môi trường: cần MySQL đang chạy đúng cấu hình application.yml (giống các test có sẵn trong project).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testEmail;
    private final String rawPassword = "123456";

    @BeforeEach
    void setup() {
        // CustomUserDetailsService lấy authority trực tiếp từ role.getName() -> phải là "ROLE_USER"
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Vai trò người dùng thông thường");
            return roleRepository.save(r);
        });

        testEmail = "flow_login_" + System.currentTimeMillis() + "@gmail.com";
        User user = new User();
        user.setFullName("Nguyen Van Login Flow");
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(userRole);
        userRepository.save(user);
    }

    @Test
    void fullLoginFlow_LoginThenAccessProtectedPage_ThenLogout_ShouldDenyAccessAfter() throws Exception {
        // ===== B1: Đăng nhập thật (đi qua Spring Security filter chain + CustomSuccessHandler thật) =====
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", testEmail)
                        .password("password", rawPassword))
                .andExpect(authenticated())
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(sessionCookie,
                "Phải có cookie 'SESSION' sau khi đăng nhập thành công (Spring Session JDBC cấp phát)");

        // ===== B2: Dùng cookie SESSION vừa đăng nhập truy cập trang yêu cầu xác thực =====
        // /view-profile đọc session.getAttribute("id") do CustomSuccessHandler set khi login thành công
        // -> nếu trả về 200 (thay vì 500/redirect) tức là phiên đăng nhập đã được thiết lập đúng.
        mockMvc.perform(get("/view-profile").cookie(sessionCookie))
                .andExpect(status().isOk());

        // ===== B3: Đăng xuất (dùng chính cookie SESSION đang có hiệu lực) =====
        mockMvc.perform(post("/logout").cookie(sessionCookie))
                .andExpect(status().is3xxRedirection());

        // ===== B4: Dùng lại cookie cũ (đã bị vô hiệu hóa phía server sau logout) truy cập lại =====
        // Sau logout, Spring Session đã xóa session tương ứng khỏi DB. Project này cấu hình
        // .invalidSessionUrl("/logout?expired") trong SecurityConfiguration -> khi phát hiện session
        // không hợp lệ, Spring Security chủ động redirect sang "/logout?expired" (không phải "/login"
        // trực tiếp) để dọn cookie/phiên rồi mới tiếp tục điều hướng - đây là hành vi ĐÚNG theo cấu
        // hình thật của project, không phải lỗi.
        mockMvc.perform(get("/view-profile").cookie(sessionCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout?expired"));
    }

    @Test
    void fullLoginFlow_WrongPassword_ShouldStayUnauthenticated_AndDenyProtectedPage() throws Exception {
        // B1: Đăng nhập sai mật khẩu -> đăng nhập thất bại
        mockMvc.perform(formLogin("/login")
                        .user("username", testEmail)
                        .password("password", "SaiMatKhau999"))
                .andExpect(unauthenticated());

        // B2: Không mang theo cookie SESSION nào (vì đăng nhập thất bại) -> vẫn cố truy cập trang yêu cầu xác thực
        mockMvc.perform(get("/view-profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}