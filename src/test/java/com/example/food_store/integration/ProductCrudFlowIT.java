package com.example.food_store.integration;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.food_store.domain.Product;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.repository.ProductRepository;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;
import com.example.food_store.service.impl.UploadService;

import jakarta.servlet.http.Cookie;

/**
 * Test LUỒNG QUẢN TRỊ SẢN PHẨM đầy đủ (end-to-end): Create -> Read -> Update -> Delete,
 * đi qua toàn bộ Spring stack thật (Controller thật -> Bean Validation thật -> ProductService thật
 * -> ProductRepository thật -> DB thật), chỉ mock UploadService để không ghi file ảnh thật ra ổ đĩa.
 *
 * Luồng:
 *   B1: Đăng nhập bằng tài khoản ADMIN thật (đi qua Spring Security thật, hasRole("ADMIN") thật)
 *   B2: POST /admin/product/create -> tạo sản phẩm thật trong DB
 *   B3: GET /admin/product/{id} -> xác nhận đọc lại đúng dữ liệu vừa tạo
 *   B4: POST /admin/product/update -> cập nhật sản phẩm thật trong DB
 *   B5: GET /admin/product/{id} -> xác nhận dữ liệu đã được cập nhật đúng
 *   B6: POST /admin/product/delete -> xóa sản phẩm thật khỏi DB
 *   B7: Xác nhận sản phẩm không còn tồn tại trong DB
 *
 * Yêu cầu môi trường: cần MySQL đang chạy đúng cấu hình application.yml.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductCrudFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UploadService uploadService; // Tránh ghi file ảnh thật ra ổ đĩa khi test

    private Cookie adminSessionCookie;
    private final String testProductName = "Flow Test Product " + System.currentTimeMillis();

    @BeforeEach
    void setup() throws Exception {
        when(uploadService.handleSaveUploadFile(any(), anyString())).thenReturn("flow-test-image.jpg");

        // CustomUserDetailsService lấy authority trực tiếp từ role.getName() -> phải là "ROLE_ADMIN"
        // để SecurityConfiguration.hasRole("ADMIN") cho phép truy cập /admin/**
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_ADMIN");
            r.setDescription("Vai trò quản trị viên");
            return roleRepository.save(r);
        });

        String adminEmail = "flow_admin_" + System.currentTimeMillis() + "@gmail.com";
        String rawPassword = "123456";
        User admin = new User();
        admin.setFullName("Admin Flow Test");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(adminRole);
        userRepository.save(admin);

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", adminEmail)
                        .password("password", rawPassword))
                .andExpect(authenticated())
                .andReturn();
        adminSessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(adminSessionCookie, "Phải có cookie 'SESSION' sau khi đăng nhập admin thành công (Spring Session JDBC)");
    }

    @Test
    void fullProductCrudFlow_CreateReadUpdateDelete_ShouldReflectInDatabase() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "productFile", "test-image.jpg", "image/jpeg", "fake-image-content".getBytes());

        // ===== B1: TẠO sản phẩm mới (Create) =====
        mockMvc.perform(multipart("/admin/product/create")
                        .file(imageFile)
                        .cookie(adminSessionCookie)
                        .param("name", testProductName)
                        .param("price", "50000")
                        .param("quantity", "10")
                        .param("detailDesc", "Mo ta chi tiet san pham test luong")
                        .param("shortDesc", "Mo ta ngan")
                        .param("source", "Viet Nam")
                        .param("unit", "Hop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));

        Product created = productRepository.findAll().stream()
                .filter(p -> testProductName.equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(created, "Sản phẩm phải được lưu thật trong DB sau bước Create");
        long productId = created.getId();
        assertEquals(50000.0, created.getPrice());

        // ===== B2: ĐỌC LẠI sản phẩm vừa tạo (xác nhận Create phản ánh đúng qua GET) =====
        mockMvc.perform(get("/admin/product/" + productId).cookie(adminSessionCookie))
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", hasProperty("name", is(testProductName))));

        // ===== B3: CẬP NHẬT sản phẩm (Update) =====
        String updatedName = testProductName + " - Updated";
        mockMvc.perform(multipart("/admin/product/update")
                        .file(imageFile)
                        .cookie(adminSessionCookie)
                        .param("id", String.valueOf(productId))
                        .param("name", updatedName)
                        .param("price", "75000")
                        .param("quantity", "20")
                        .param("detailDesc", "Mo ta chi tiet sau khi update")
                        .param("shortDesc", "Mo ta ngan sau khi update")
                        .param("source", "Viet Nam")
                        .param("unit", "Hop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));

        // ===== B4: ĐỌC LẠI để xác nhận Update đã phản ánh đúng trong DB =====
        Optional<Product> afterUpdate = productRepository.findById(productId);
        assertTrue(afterUpdate.isPresent(), "Sản phẩm vẫn phải tồn tại sau bước Update");
        assertEquals(updatedName, afterUpdate.get().getName());
        assertEquals(75000.0, afterUpdate.get().getPrice());
        assertEquals(20L, afterUpdate.get().getQuantity());

        // ===== B5: XÓA sản phẩm (Delete) =====
        mockMvc.perform(post("/admin/product/delete")
                        .cookie(adminSessionCookie)
                        .param("id", String.valueOf(productId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));

        // ===== B6: Xác nhận sản phẩm không còn tồn tại thật trong DB =====
        assertTrue(productRepository.findById(productId).isEmpty(),
                "Sản phẩm phải bị xóa thật khỏi DB sau bước Delete");
    }

    @Test
    void fullProductCrudFlow_NonAdminUser_ShouldBeDeniedAccess() throws Exception {
        // Test bổ sung cho DEL-TC04 (đề xuất mới) - dùng cho cả Create/Update/Delete:
        // tài khoản không phải ADMIN không được phép truy cập /admin/product/**
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Vai trò người dùng thông thường");
            return roleRepository.save(r);
        });
        String normalEmail = "flow_normal_user_" + System.currentTimeMillis() + "@gmail.com";
        String rawPassword = "123456";
        User normalUser = new User();
        normalUser.setFullName("Nguyen Van Thuong");
        normalUser.setEmail(normalEmail);
        normalUser.setPassword(passwordEncoder.encode(rawPassword));
        normalUser.setRole(userRole);
        userRepository.save(normalUser);

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", normalEmail)
                        .password("password", rawPassword))
                .andExpect(authenticated())
                .andReturn();
        Cookie userSessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(userSessionCookie, "Phải có cookie 'SESSION' sau khi đăng nhập USER thành công");

        // Cố truy cập trang quản trị sản phẩm bằng tài khoản USER thường
        mockMvc.perform(get("/admin/product").cookie(userSessionCookie))
                .andExpect(status().isForbidden());
    }
}