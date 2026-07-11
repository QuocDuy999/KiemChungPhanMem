package com.example.food_store.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.Order;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.repository.CartRepository;
import com.example.food_store.repository.OrderRepository;
import com.example.food_store.repository.ProductRepository;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;

/**
 * Test LUỒNG THANH TOÁN COD đầy đủ (end-to-end), đi qua toàn bộ Spring stack thật:
 * Đăng nhập thật -> Thêm sản phẩm vào giỏ hàng thật -> Đặt hàng COD thật -> Kiểm tra Order/Cart thật trong DB.
 *
 * Luồng thanh toán VNPay (online) không được test end-to-end ở đây vì cần gọi ra cổng thanh toán
 * bên ngoài (sandbox.vnpayment.vn) - việc đó nên test riêng ở tầng Service (xem VNPAYServiceTest có sẵn).
 *
 * Luồng:
 *   B1: Đăng nhập
 *   B2: POST /add-product-to-cart/{id} -> xác nhận Cart được tạo tự động, sum = 1
 *   B3: POST /place-order (paymentMethod=COD) -> xác nhận redirect /afterOrder
 *   B4: GET /afterOrder (đúng như trình duyệt thật sẽ tự chuyển tới) -> 200 OK
 *   B5: Kiểm tra dữ liệu thật trong DB: Order đã được tạo đúng, Cart đã bị xóa (theo đúng logic
 *       ProductService.handlePlaceOrder() hiện tại: xóa toàn bộ cart sau khi đặt hàng thành công)
 *
 * Yêu cầu môi trường: cần MySQL đang chạy đúng cấu hình application.yml. KHÔNG cần RabbitMQ
 * (EmailProducer được @MockBean) và KHÔNG cần VNPay vì test này chỉ đi luồng COD.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;
    // Dùng để "quên" cache Hibernate giữa các bước, buộc đọc lại DB thật - mô phỏng đúng việc
    // mỗi request HTTP thật (add-to-cart, rồi place-order) có vùng nhớ persistence context RIÊNG BIỆT,
    // tránh việc Cart.getCartDetails() trả về giá trị null/cũ do object Java bị giữ nguyên
    // xuyên suốt cả @Transactional test (đây là hạn chế kỹ thuật của cách viết Integration Test
    // dùng chung 1 transaction cho nhiều request mô phỏng, KHÔNG phải lỗi thật của ứng dụng).

    @MockBean
    private EmailProducer emailProducer;

    private String testEmail;
    private final String rawPassword = "123456";
    private long productId;

    @BeforeEach
    void setup() {
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Vai trò người dùng thông thường");
            return roleRepository.save(r);
        });

        testEmail = "flow_payment_" + System.currentTimeMillis() + "@gmail.com";
        User user = new User();
        user.setFullName("Nguyen Van Payment Flow");
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(userRole);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Flow Test Product " + System.currentTimeMillis());
        product.setPrice(25000);
        product.setQuantity(100);
        product.setDetailDesc("Mo ta chi tiet san pham test luong thanh toan");
        product.setShortDesc("Mo ta ngan");
        product.setSource("Viet Nam");
        product.setUnit("Cai");
        product.setImage("default.jpg");
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void fullPaymentFlow_COD_ShouldCreateOrderAndClearCart() throws Exception {
        // ===== B1: Đăng nhập thật =====
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", testEmail)
                        .password("password", rawPassword))
                .andExpect(authenticated())
                .andReturn();
        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(sessionCookie, "Phải có cookie 'SESSION' sau khi đăng nhập thành công (Spring Session JDBC)");

        // ===== B2: Thêm sản phẩm vào giỏ hàng =====
        mockMvc.perform(post("/add-product-to-cart/" + productId).cookie(sessionCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        User user = userRepository.findByEmail(testEmail);
        Cart cart = cartRepository.findByUser(user);
        assertNotNull(cart, "Giỏ hàng phải được tự động tạo khi thêm sản phẩm đầu tiên");
        assertEquals(1, cart.getSum(), "Giỏ hàng phải có 1 sản phẩm sau bước thêm giỏ hàng");

        // Buộc Hibernate "quên" cache và đọc lại DB thật ở bước tiếp theo (xem giải thích ở khai báo
        // entityManager phía trên) - nếu không, cart.getCartDetails() trong handlePlaceOrder() sẽ
        // trả về null (vì đang giữ nguyên object Java rỗng từ lúc mới new Cart()), khiến đơn hàng
        // không được tạo dù luồng HTTP vẫn báo thành công (302) - đây là artefact riêng của cách
        // test nhiều bước trong 1 @Transactional, không phải lỗi thật của handlePlaceOrder().
        entityManager.flush();
        entityManager.clear();

        // ===== B3: Đặt hàng COD =====
        mockMvc.perform(post("/place-order").cookie(sessionCookie)
                        .param("receiverName", "Nguyen Van Payment Flow")
                        .param("receiverAddress", "123 Duong ABC, Quan 1")
                        .param("receiverPhone", "0901234567")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "25000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));

        // ===== B4: Trang sau khi đặt hàng - đúng như trình duyệt thật sẽ tự chuyển tới =====
        mockMvc.perform(get("/afterOrder").cookie(sessionCookie))
                .andExpect(status().isOk());

        // ===== B5: Kiểm tra dữ liệu THẬT trong DB sau khi hoàn tất luồng =====
        List<Order> orders = orderRepository.findByUser(user);
        assertFalse(orders.isEmpty(), "Đơn hàng phải được lưu thật trong DB sau luồng thanh toán");
        Order order = orders.get(orders.size() - 1);
        assertEquals("COD", order.getPaymentMethod());
        assertEquals("123 Duong ABC, Quan 1", order.getReceiverAddress());
        assertEquals("0901234567", order.getReceiverPhone());
        assertEquals(25000.0, order.getTotalPrice());

        // Theo đúng logic hiện tại của ProductService.handlePlaceOrder(): cart bị xóa hẳn sau khi đặt hàng
        assertNull(cartRepository.findByUser(user),
                "Giỏ hàng phải bị xóa hoàn toàn sau khi đặt hàng COD thành công (đúng theo logic hiện tại)");
    }

    @Test
    void fullPaymentFlow_EmptyCart_ShouldNotCreateOrder() throws Exception {
        // Test luồng khi KHÔNG có sản phẩm nào trong giỏ hàng trước khi đặt hàng (EP19/EP20 - đề xuất mới)
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", testEmail)
                        .password("password", rawPassword))
                .andExpect(authenticated())
                .andReturn();
        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(sessionCookie, "Phải có cookie 'SESSION' sau khi đăng nhập thành công (Spring Session JDBC)");

        // Không thêm sản phẩm vào giỏ (cart == null) -> đặt hàng luôn
        mockMvc.perform(post("/place-order").cookie(sessionCookie)
                        .param("receiverName", "Nguyen Van Empty Cart")
                        .param("receiverAddress", "123 Duong ABC")
                        .param("receiverPhone", "0901234567")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));

        User user = userRepository.findByEmail(testEmail);
        List<Order> orders = orderRepository.findByUser(user);
        assertTrue(orders.isEmpty(),
                "Vì cart == null (chưa từng thêm sản phẩm), handlePlaceOrder() hiện tại sẽ return êm ru "
                        + "và KHÔNG tạo Order nào - ghi nhận đúng hành vi hiện tại của hệ thống (xem PAY-TC20 đề xuất)");
    }
}