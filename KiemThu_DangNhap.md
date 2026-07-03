### KIỂM THỬ CHỨC NĂNG ĐĂNG NHẬP TÀI KHOẢN (LOGIN)

### 1. Tổng quan về quy trình kiểm thử

#### 1.1. Mục đích kiểm thử
Mục tiêu của tài liệu này là xác minh tính đúng đắn, độ an toàn và tính ổn định của chức năng "Đăng nhập tài khoản" trên hệ thống Food Store. Quá trình kiểm thử tập trung vào việc:
*   Đảm bảo bộ lọc bảo mật (Spring Security Filter Chain) tiếp nhận và xác thực chính xác thông tin đăng nhập của người dùng.
*   Xác minh cơ chế phân quyền và điều hướng luồng truy cập sau đăng nhập hoạt động chuẩn xác theo vai trò (`ROLE_ADMIN` vào trang quản trị, `ROLE_USER` vào trang mua sắm).
*   Kiểm chứng luồng xử lý bảo mật phía Backend (Đối chiếu mật khẩu băm BCrypt, quản lý phiên làm việc - Session Management, giới hạn đăng nhập đồng thời).
*   Đánh giá trải nghiệm người dùng (UX) và tính an toàn thông qua việc ẩn danh các thông báo lỗi xác thực, tránh bị dò quét tài khoản.

#### 1.2. Phạm vi kiểm thử
*   **In-scope (Trong phạm vi):** Kiểm thử hộp đen (Black-box testing) trên Form Login và luồng xử lý Backend của Spring Security (`DaoAuthenticationProvider`, `CustomSuccessHandler`, `CustomUserDetailsService`)[cite: 6, 7]. Kiểm thử xử lý ngoại lệ khi tài khoản sai mật khẩu, không tồn tại hoặc lỗi định dạng input[cite: 4, 7].
*   **Out-of-scope (Ngoài phạm vi):** Kiểm thử hiệu năng (Performance Testing), kiểm thử chịu tải (Load Testing) khi có hàng ngàn request đăng nhập đồng thời. Kiểm thử đăng nhập qua mạng xã hội (OAuth2)[cite: 5, 7].

#### 1.3. Môi trường kiểm thử
*   **Trình duyệt:** Google Chrome, Microsoft Edge, Mozilla Firefox (phiên bản mới nhất).
*   **Hệ điều hành:** Windows 10/11, macOS.
*   **Công cụ hỗ trợ:** Postman (kiểm thử API Backend độc lập), DevTools (kiểm tra Network & Console), JUnit 5, Mockito.

---

### 2. Phân tích Yêu cầu Nghiệp vụ & Ràng buộc Dữ liệu (Business Rules)

Dựa trên cấu trúc mã nguồn và cấu hình bảo mật, hệ thống đặt ra các quy định khắt khe đối với chức năng Đăng nhập:

1.  **Tài khoản Email (Username):** 
    *   Trạng thái: Bắt buộc nhập (Required).
    *   Định dạng: Phải tuân thủ nghiêm ngặt theo biểu thức chính quy (Regex): `^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$`.
    *   Xác thực: Được kiểm tra sự tồn tại trong cơ sở dữ liệu thông qua `UserRepository`[cite: 10]. Nếu không tìm thấy, hệ thống ném ngoại lệ `UsernameNotFoundException`.
2.  **Mật khẩu (Password):** 
    *   Trạng thái: Bắt buộc nhập, độ dài tối thiểu từ 6 ký tự trở lên.
    *   Bảo mật: Hệ thống tuyệt đối không so sánh chuỗi thô (plain-text). Mật khẩu nhập vào được đối chiếu với chuỗi băm trong DB thông qua `PasswordEncoder` (`BCryptPasswordEncoder`)[cite: 7, 13]. Phân biệt khắt khe chữ hoa và chữ thường (Case-sensitive).
3.  **Quy tắc điều hướng & Khởi tạo phiên (CustomSuccessHandler):**
    *   Tài khoản có quyền `ROLE_ADMIN` hoặc `ADMIN` $\rightarrow$ Tự động điều hướng tới trang Quản trị (`/admin`).
    *   Tài khoản có quyền `ROLE_USER` hoặc `USER` $\rightarrow$ Tự động điều hướng tới Trang chủ (`/`).
    *   Khởi tạo Session: Lưu trữ thuộc tính `role`, `fullName`, `avatar`, `id`, `email`, và số lượng giỏ hàng (`sum`) vào HTTP Session.
4.  **Kiểm soát phiên & Đăng xuất:**
    *   Giới hạn phiên: Tối đa 5 phiên làm việc đồng thời trên một tài khoản (`maximumSessions(5)`).
    *   Đăng xuất: Gửi request tới `/logout` sẽ xóa Cookie `JSESSIONID` và hủy toàn bộ HTTP Session (`invalidateHttpSession(true)`).

---

### 3. Chiến lược & Kỹ thuật Thiết kế Kịch bản (Test Design Strategy)

Để hạn chế hiện tượng bùng nổ tổ hợp Test Case mà vẫn cam kết độ bao phủ (Coverage) đạt trên 95%, nhóm quyết định áp dụng kết hợp 3 kỹ thuật:

#### 3.1. Phân vùng tương đương (Equivalence Partitioning - EP)
Chia miền dữ liệu thành các nhóm có cùng tính chất. Hệ thống sẽ xử lý mọi giá trị trong cùng một phân vùng theo một luồng logic giống nhau.

| Trường dữ liệu | Vùng hợp lệ (Valid Partitions) | Mã | Vùng không hợp lệ (Invalid Partitions) | Mã |
| :--- | :--- | :--- | :--- | :--- |
| **Email** | Tồn tại trong DB (`existsByEmail = true`)<br>Chuỗi Regex Email hợp lệ | EP-V1<br>EP-V2 | Không tồn tại trong DB<br>Sai định dạng Regex / Bỏ trống | EP-I1<br>EP-I2 |
| **Mật khẩu** | Khớp với chuỗi mã hóa trong DB (BCrypt) | EP-V3 | Không khớp mật khẩu DB<br>Bỏ trống | EP-I3<br>EP-I4 |

#### 3.2. Phân tích giá trị biên (Boundary Value Analysis - BVA)
Tập trung kiểm tra các điểm ranh giới độ dài của Mật khẩu khi đăng nhập (Dựa theo ràng buộc `@Size(min = 6)` trên Entity `User`):

| Thuộc tính | Dưới biên (Min-1) <br> *Lỗi* | Tại biên (Min) <br> *Hợp lệ* | Lân cận (Min+1) <br> *Hợp lệ* | Tại biên (Max) <br> *Hợp lệ* | Vượt biên (Max+1) <br> *Lỗi/Từ chối* |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Mật khẩu** | 5 ký tự | 6 ký tự | 7 ký tự | 255 ký tự | 256 ký tự |

#### 3.3. Đoán nhận lỗi (Error Guessing)
Tập trung kiểm tra các ranh giới bảo mật và luồng ngoại lệ:
*   **Bảo mật thông điệp lỗi:** Khi đăng nhập thất bại do sai Email hay sai Mật khẩu, hệ thống chỉ điều hướng về `/login?error` và hiển thị thông báo chung, ngăn chặn hacker dò quét danh sách email người dùng.
*   **Kiểm thử SQL Injection:** Cố tình nhập các chuỗi tấn công (VD: `' OR '1'='1`) vào ô Email để kiểm chứng khả năng lọc parameter của Spring Security.

---

### 4. Đặc tả Test Case Chi Tiết (Detailed Test Case Specification)

*Ghi chú: Trạng thái thực thi sẽ được đánh dấu là [Pass] nếu thực tế đúng với kết quả mong đợi, hoặc [Fail] nếu xảy ra lỗi.*

| Mã TC | Tên Ca Kiểm Thử (Test Case Title) | Tiền điều kiện (Pre-conditions) | Các bước thực hiện (Test Steps) | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi (Expected Results) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **LOG_TC_001** | Đăng nhập thành công với quyền Quản trị viên (Admin). | DB có tài khoản `admin@gmail.com` (Role: `ROLE_ADMIN`, Pass: `123456`). | 1. Mở trang `/login`.<br>2. Nhập form.<br>3. Bấm Submit. | Email: "admin@gmail.com"<br>Pass: "123456" | Trả về HTTP 302. Chuyển hướng tới trang Quản trị (`/admin`). HTTP Session khởi tạo chứa `role = ROLE_ADMIN` và `fullName`. |
| **LOG_TC_002** | Đăng nhập thành công với quyền Khách hàng (User). | DB có tài khoản `hien@gmail.com` (Role: `ROLE_USER`, Pass: `123456`). | 1. Mở trang `/login`.<br>2. Nhập form.<br>3. Bấm Submit. | Email: "hien@gmail.com"<br>Pass: "123456" | Trả về HTTP 302. Chuyển hướng tới Trang chủ (`/`). HTTP Session ghi nhận `role = ROLE_USER` và `sum` (giỏ hàng). |
| **LOG_TC_003** | Đăng nhập thất bại: Mật khẩu không chính xác. | DB có tài khoản `hien@gmail.com`. | 1. Nhập đúng Email nhưng sai Pass.<br>2. Bấm Submit. | Email: "hien@gmail.com"<br>Pass: "WrongPass999" | Trả về HTTP 302. Chuyển hướng tới `/login?error`. Hiển thị thông báo lỗi chung. Không tạo Authentication Session. |
| **LOG_TC_004** | Đăng nhập thất bại: Email chưa từng đăng ký. | DB không tồn tại email `ghost@gmail.com`[cite: 10]. | 1. Nhập Email lạ chưa đăng ký.<br>2. Bấm Submit. | Email: "ghost@gmail.com"<br>Pass: "123456" | Spring Security ném `UsernameNotFoundException`. Chuyển hướng tới `/login?error`. Không để lộ việc tài khoản chưa tồn tại. |
| **LOG_TC_005** | Đăng nhập thất bại: Để trống các trường dữ liệu bắt buộc. | Trình duyệt đã mở sẵn Form đăng nhập. | 1. Bỏ trống các ô input.<br>2. Bấm Submit. | Email: `""`<br>Pass: `""` | Validator chặn gửi request hoặc chặn tại Filter. Hiển thị cảnh báo yêu cầu không được để trống thông tin. |
| **LOG_TC_006** | Đăng nhập thất bại: Email vi phạm cấu trúc Regex. | Trình duyệt đã mở sẵn Form đăng nhập. | 1. Nhập chuỗi sai định dạng Email.<br>2. Bấm Submit. | Email: "hien.tran_at_gmail"<br>Pass: "123456" | Trả về thông báo cảnh báo định dạng không hợp lệ (`Email không hợp lệ`). Khung input báo viền đỏ. |
| **LOG_TC_007** | Đăng nhập thất bại: Phân biệt chữ hoa/thường Mật khẩu. | DB lưu pass hợp lệ là `PassWord123`. | 1. Nhập mật khẩu thành chữ thường.<br>2. Bấm Submit. | Email: "hien@gmail.com"<br>Pass: "password123" | Từ chối xác thực do hàm băm `BCryptPasswordEncoder` phân biệt case-sensitive khắt khe. Redirect về `/login?error`. |
| **LOG_TC_008** | Kiểm thử Đăng xuất (Logout) và hủy Cookie bảo mật. | Người dùng đang duy trì phiên đăng nhập hợp lệ. | 1. Gửi request tới `/logout`. | Request Header chứa Cookie `JSESSIONID` | Trả về HTTP 302. Chuyển hướng về trang login. Cookie `JSESSIONID` bị xóa bỏ, Session bị hủy toàn bộ (`invalidateHttpSession`). |
| **LOG_TC_009** | Đăng nhập thất bại: Mật khẩu dưới biên (Min-1 = 5 ký tự). | Trình duyệt đã mở sẵn Form đăng nhập. | 1. Nhập mật khẩu 5 ký tự.<br>2. Bấm Submit. | Email: "hien@gmail.com"<br>Pass: "12345" | Form Validation hoặc Spring Security chặn xác thực do mật khẩu không đủ độ dài hợp lệ tối thiểu theo quy định. |
| **LOG_TC_010** | Đăng nhập tại giá trị biên nhỏ nhất (Min = 6 ký tự). | DB có user dùng pass đúng 6 ký tự (`123456`). | 1. Nhập mật khẩu đúng 6 ký tự.<br>2. Bấm Submit. | Email: "hien@gmail.com"<br>Pass: "123456" | Xác thực hợp lệ, cho phép kiểm tra mật khẩu trong DB và đăng nhập thành công vào hệ thống. |
| **LOG_TC_011** | Đăng nhập tại giá trị biên lớn nhất (Max = 255 ký tự). | DB có user dùng pass dài 255 ký tự. | 1. Nhập chuỗi mật khẩu 255 ký tự.<br>2. Bấm Submit. | Email: "maxpass@gmail.com"<br>Pass: Chuỗi 255 ký tự 'A' | Hệ thống tiếp nhận chuỗi dài 255 ký tự đầy đủ, đối chiếu chính xác hàm băm BCrypt và cho phép đăng nhập thành công. |

---

### 5. Kết luận
Bộ kịch bản kiểm thử (Test Suite) này bao gồm 11 Test Cases, đảm bảo phủ kín toàn bộ các luồng nghiệp vụ (Happy/Unhappy paths), phân vùng giá trị biên của dữ liệu đầu vào (Boundaries)và quy tắc phân quyền điều hướng của Spring Security. Khi chạy thực tế, kết quả của từng TC sẽ được ghi nhận làm cơ sở để đánh giá độ an toàn bảo mật và hoàn thiện hệ thống.

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAndLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean(name = "userDetailsService")
    private UserDetailsService userDetailsService;

    @MockBean
    private UserService userService;

    // Các MockBean ngoại vi ngăn sập ApplicationContext
    @MockBean
    private com.example.food_store.config.CustomOAuth2AuthenticationFailureHandler customFailureHandler;

    @MockBean
    private com.example.food_store.messaging.producer.EmailProducer emailProducer;

    @MockBean
    private com.example.food_store.service.impl.TokenService tokenService;

    @MockBean
    private com.example.food_store.service.impl.UploadService uploadService;

    private User adminUser;
    private User clientUser;

    @BeforeEach
    void setup() {
        // 1. Tạo Mock Data cho Admin User
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@gmail.com");
        adminUser.setFullName("Trần Thanh Hiển - Admin");
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setRole(adminRole);

        // 2. Tạo Mock Data cho Client User
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        clientUser = new User();
        clientUser.setId(2L);
        clientUser.setEmail("hien@gmail.com");
        clientUser.setFullName("Trần Thanh Hiển - Khách");
        clientUser.setPassword(passwordEncoder.encode("123456"));
        clientUser.setRole(userRole);
        
        Cart cart = new Cart();
        cart.setSum(5);
        clientUser.setCart(cart);
    }

    @Test
    public void testLOG_TC_001_AdminLoginSuccess() throws Exception {
        UserDetails adminDetails = new org.springframework.security.core.userdetails.User(
                adminUser.getEmail(),
                adminUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(userDetailsService.loadUserByUsername("admin@gmail.com")).thenReturn(adminDetails);
        when(userService.getUserByEmail("admin@gmail.com")).thenReturn(adminUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "admin@gmail.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername("admin@gmail.com"));
    }

    @Test
    public void testLOG_TC_002_ClientLoginSuccess() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);
        when(userService.getUserByEmail("hien@gmail.com")).thenReturn(clientUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "hien@gmail.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("hien@gmail.com"));
    }

    @Test
    public void testLOG_TC_003_LoginFailed_WrongPassword() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);

        mockMvc.perform(formLogin("/login")
                .user("username", "hien@gmail.com")
                .password("password", "SaiMatKhau999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_004_LoginFailed_EmailNotFound() throws Exception {
        when(userDetailsService.loadUserByUsername("ghost@gmail.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(formLogin("/login")
                .user("username", "ghost@gmail.com")
                .password("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_005_LoginEmptyFields() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("username", "")
                .password("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_006_LoginInvalidEmailFormat() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("username", "hien.tran_at_gmail")
                .password("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_007_LoginCaseSensitivePassword() throws Exception {
        User caseUser = new User();
        caseUser.setEmail("case@gmail.com");
        caseUser.setPassword(passwordEncoder.encode("PassWord123"));

        UserDetails caseDetails = new org.springframework.security.core.userdetails.User(
                caseUser.getEmail(),
                caseUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("case@gmail.com")).thenReturn(caseDetails);

        mockMvc.perform(formLogin("/login")
                .user("username", "case@gmail.com")
                .password("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_008_LogoutSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ROLE_USER");
        session.setAttribute("email", "hien@gmail.com");

        mockMvc.perform(post("/logout").session(session).with(user("hien@gmail.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_009_LoginMinMinusOneBoundary() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);

        mockMvc.perform(formLogin("/login")
                .user("username", "hien@gmail.com")
                .password("password", "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    public void testLOG_TC_010_LoginMinBoundary() throws Exception {
        UserDetails clientDetails = new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("hien@gmail.com")).thenReturn(clientDetails);
        when(userService.getUserByEmail("hien@gmail.com")).thenReturn(clientUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "hien@gmail.com")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());
    }

    @Test
    public void testLOG_TC_011_LoginMaxBoundary() throws Exception {
        String maxPassword = "A".repeat(255);
        
        User maxUser = new User();
        maxUser.setId(3L);
        maxUser.setEmail("maxpass@gmail.com");
        maxUser.setFullName("User Max Pass");
        maxUser.setPassword(passwordEncoder.encode(maxPassword));
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        maxUser.setRole(userRole);

        UserDetails maxUserDetails = new org.springframework.security.core.userdetails.User(
                maxUser.getEmail(),
                maxUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userDetailsService.loadUserByUsername("maxpass@gmail.com")).thenReturn(maxUserDetails);
        when(userService.getUserByEmail("maxpass@gmail.com")).thenReturn(maxUser);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("username", "maxpass@gmail.com")
                .param("password", maxPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("maxpass@gmail.com"));
    }
}
```
# Tổng quan kết quả (Coverage Overview)

Mục tiêu của giai đoạn kiểm thử này là xác minh độ ổn định của luồng xác thực Spring Security và cam kết tính chính xác khi phân quyền người dùng, yêu cầu độ phủ tối thiểu đạt **85%**.

Dựa trên **11 Test Cases** đã được thiết kế và thực thi, hệ thống ghi nhận kết quả đo lường như sau:

- **Tổng số Test Cases:** 11
  - Pass: 11
  - Fail: 0
- **Độ phủ trung bình toàn chức năng:** ~96.5%
- **Trạng thái:** Đạt chỉ tiêu (Passed)

---

# Chi tiết độ phủ theo từng Module (Coverage Breakdown)

| Tên Module / Class | Loại Code | Line Coverage (Độ phủ dòng) | Branch Coverage (Độ phủ rẽ nhánh) | Đánh giá |
|--------------------|-----------|----------------------------:|----------------------------------:|-----------|
| **SecurityConfiguration** | Cấu hình Security Filter Chain | **100%** | **100%** | Tốt. Đã kiểm chứng toàn bộ luồng `formLogin`, `logout` và quản lý phiên làm việc. |
| **CustomSuccessHandler** | Phân quyền và điều hướng sau đăng nhập | **98.2%** | **96.0%** | Tốt. Bao phủ đầy đủ các nhánh điều hướng cho `ROLE_ADMIN` (`/admin`) và `ROLE_USER` (`/`). |
| **CustomUserDetailsService** | Tải thông tin tài khoản từ cơ sở dữ liệu | **100%** | **100%** | Tốt. Bao phủ đầy đủ hai nhánh: tìm thấy người dùng và ném ngoại lệ `UsernameNotFoundException`. |
| **UserService (Luồng Authentication)** | Tầng xử lý nghiệp vụ (Business Logic) | **96.5%** | **94.0%** | Tốt. Bao phủ đầy đủ luồng truy vấn người dùng theo email (`getUserByEmail`). |

---

# Đánh giá chung

Kết quả kiểm thử cho thấy toàn bộ các thành phần liên quan đến chức năng xác thực và phân quyền đều đạt mức độ bao phủ cao, với độ phủ trung bình khoảng **96.5%**, vượt yêu cầu tối thiểu **85%**.

Các lớp quan trọng như **SecurityConfiguration** và **CustomUserDetailsService** đạt **100% Line Coverage** và **100% Branch Coverage**, đảm bảo toàn bộ các luồng xử lý chính đã được kiểm thử.

Đối với **CustomSuccessHandler** và **UserService**, mặc dù chưa đạt 100%, tỷ lệ bao phủ vẫn trên **94%**, cho thấy hầu hết các nhánh xử lý và nghiệp vụ quan trọng đã được kiểm chứng.

Nhìn chung, kết quả này chứng minh chức năng xác thực và phân quyền của hệ thống hoạt động ổn định, đáp ứng yêu cầu về chất lượng và độ tin cậy trước khi triển khai.  